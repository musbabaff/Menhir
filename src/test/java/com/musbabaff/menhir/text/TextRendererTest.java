/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextRendererTest {

    /** Flattens a component into "text|colour|bold" entries for easy assertions. */
    private static List<String> flatten(Component component) {
        List<String> out = new ArrayList<>();
        flatten(component, null, false, out);
        return out;
    }

    private static void flatten(Component c, TextColor inheritedColor, boolean inheritedBold, List<String> out) {
        TextColor color = c.color() != null ? c.color() : inheritedColor;
        boolean bold = c.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE
                || (inheritedBold && c.decoration(TextDecoration.BOLD) != TextDecoration.State.FALSE);
        if (c instanceof TextComponent text && !text.content().isEmpty()) {
            out.add(text.content() + "|" + (color == null ? "-" : color.asHexString()) + "|" + (bold ? "b" : "-"));
        }
        for (Component child : c.children()) flatten(child, color, bold, out);
    }

    @Test
    @DisplayName("legacy colour codes are converted")
    void legacyCodes() {
        Component c = TextRenderer.render("&7Gray &aGreen");
        assertEquals(List.of("Gray |#aaaaaa|-", "Green|#55ff55|-"), flatten(c));
    }

    @Test
    @DisplayName("legacy formatting codes are converted and a colour resets them")
    void legacyFormatting() {
        Component c = TextRenderer.render("&7&lTop &7Name");
        assertEquals(List.of("Top |#aaaaaa|b", "Name|#aaaaaa|-"), flatten(c));
    }

    @Test
    @DisplayName("&#RRGGBB, {#RRGGBB} and bare #RRGGBB hex colours are supported")
    void hexVariants() {
        assertEquals(List.of("A|#2c74b3|-"), flatten(TextRenderer.render("&#2C74B3A")));
        assertEquals(List.of("A|#2c74b3|-"), flatten(TextRenderer.render("{#2C74B3}A")));
        assertEquals(List.of("A|#2c74b3|-"), flatten(TextRenderer.render("#2C74B3A")));
        assertEquals(List.of("A|#2c74b3|b"), flatten(TextRenderer.render("#2C74B3&lA")));
    }

    @Test
    @DisplayName("per-character hex gradients keep one colour per character")
    void perCharacterHex() {
        Component c = TextRenderer.render("&#FF0000M&#FF1100e&#FF2200n");
        assertEquals(List.of("M|#ff0000|-", "e|#ff1100|-", "n|#ff2200|-"), flatten(c));
    }

    @Test
    @DisplayName("MiniMessage gradient with bold renders every character bold with its own colour")
    void miniMessageGradient() {
        Component c = TextRenderer.render("<gradient:#FF4500:#FFD700><b>MENHIR</b></gradient>");
        List<String> parts = flatten(c);
        assertEquals(6, parts.size());
        assertTrue(parts.stream().allMatch(p -> p.endsWith("|b")), parts.toString());
        assertEquals("M|#ff4500|b", parts.get(0));
        assertEquals("R|#ffd700|b", parts.get(5));
    }

    @Test
    @DisplayName("legacy codes inside MiniMessage tags inherit the surrounding style")
    void legacyInsideMiniMessage() {
        Component c = TextRenderer.render("<b>&7Gray</b>");
        assertEquals(List.of("Gray|#aaaaaa|b"), flatten(c));
    }

    @Test
    @DisplayName("IridiumColorAPI <#A>text</#B> gradient becomes a MiniMessage gradient")
    void iridiumGradient() {
        Component c = TextRenderer.render("<#FF0000>ab</#0000FF>");
        List<String> parts = flatten(c);
        assertEquals(2, parts.size());
        assertEquals("a|#ff0000|-", parts.get(0));
        assertEquals("b|#0000ff|-", parts.get(1));
    }

    @Test
    @DisplayName("section-sign codes (as returned by PlaceholderAPI) are handled")
    void sectionSign() {
        assertEquals(List.of("Red|#ff5555|-"), flatten(TextRenderer.render("§cRed")));
    }

    @Test
    @DisplayName("plain text strips every kind of formatting")
    void strip() {
        assertEquals("Gray Green", TextRenderer.strip("&7Gray <b>&aGreen</b>"));
        assertEquals("", TextRenderer.strip("&c"));
    }

    @Test
    @DisplayName("toLegacy produces section codes with hex support")
    void toLegacy() {
        String legacy = TextRenderer.toLegacy("&#2C74B3Hi");
        assertEquals("§x§2§c§7§4§b§3Hi", legacy);
        assertEquals(NamedTextColor.GRAY,
                LegacyComponentSerializer.legacySection().deserialize(TextRenderer.toLegacy("&7x")).color());
    }

    @Test
    @DisplayName("null and empty input render to an empty component")
    void emptyInput() {
        assertEquals(Component.empty(), TextRenderer.render((String) null));
        assertEquals(Component.empty(), TextRenderer.render(""));
    }

}
