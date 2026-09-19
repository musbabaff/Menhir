/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the text formats used in Menhir configuration files into Adventure components.
 *
 * <p>Supported input formats (all may be mixed inside a single string):</p>
 * <ul>
 *   <li>MiniMessage tags: {@code <gradient:#FF4500:#FFD700><b>text</b></gradient>}, {@code <#RRGGBB>}, ...</li>
 *   <li>Legacy colour and formatting codes: {@code &a &7 &l &m ...} (also with the {@code §} character)</li>
 *   <li>Hex colours: {@code &#RRGGBB}, {@code {#RRGGBB}} and bare {@code #RRGGBB}
 *       (per-character "gradients" written as consecutive hex codes work as expected)</li>
 *   <li>MineBlocks / IridiumColorAPI gradients: {@code <#RRGGBB>text</#RRGGBB>} and {@code <RAINBOWn>text</RAINBOW>}</li>
 * </ul>
 *
 * <p>Processing order: MiniMessage is parsed first, then every text node of the resulting
 * component tree is passed through the legacy/hex converter. The legacy pass therefore
 * inherits any MiniMessage styling that surrounds it.</p>
 */
public final class TextRenderer {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /** Legacy deserializer for {@code &} codes and {@code &#RRGGBB} hex colours. */
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    /** Legacy serializer producing {@code §} codes (with {@code §x} hex format). */
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    /** {@code <#A>text</#B>} or {@code {#A}text{/#B}} — IridiumColorAPI gradient. */
    private static final Pattern IRIDIUM_GRADIENT = Pattern.compile("[<{]#([A-Fa-f0-9]{6})[>}](.*?)[<{]/#([A-Fa-f0-9]{6})[>}]");
    /** {@code <RAINBOW1>text</RAINBOW>} — IridiumColorAPI rainbow. */
    private static final Pattern IRIDIUM_RAINBOW = Pattern.compile("<RAINBOW([0-9]{1,3})>(.*?)</RAINBOW>");
    /** {@code {#RRGGBB}} hex. */
    private static final Pattern BRACE_HEX = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    /** Bare {@code #RRGGBB} hex, unless it is already part of a MiniMessage tag, gradient parameter or {@code &#} code. */
    private static final Pattern BARE_HEX = Pattern.compile("(?<![<:&§])#([A-Fa-f0-9]{6})");
    /** Detects legacy codes that still need converting after the MiniMessage pass. */
    private static final Pattern LEGACY_CODE = Pattern.compile("[&§](?:[0-9a-fA-Fk-oK-OrRxX]|#[A-Fa-f0-9]{6})");

    private TextRenderer() {
    }

    /**
     * Renders a configuration string into a component.
     *
     * @param raw the raw text; {@code null} is treated as an empty string
     * @return the rendered component (never {@code null})
     */
    public static Component render(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        String normalized = normalize(raw);
        Component parsed = MINI_MESSAGE.deserialize(normalized);
        return applyLegacy(parsed);
    }

    /**
     * Renders every string of the list.
     */
    public static List<Component> render(List<String> raw) {
        List<Component> result = new ArrayList<>(raw.size());
        for (String line : raw) result.add(render(line));
        return result;
    }

    /**
     * Renders a string and serializes it back to a legacy {@code §} string
     * (hex colours use the {@code §x§R§R§G§G§B§B} format). Used for APIs that only accept strings.
     */
    public static String toLegacy(String raw) {
        return LEGACY_SECTION.serialize(render(raw));
    }

    /**
     * Plain text of a component, with all formatting removed.
     */
    public static String plain(Component component) {
        return PLAIN.serialize(component);
    }

    /**
     * Plain text of a raw configuration string, with all formatting removed.
     */
    public static String strip(String raw) {
        return plain(render(raw));
    }

    /**
     * Rewrites the non-MiniMessage colour syntaxes into a form the two parsers understand:
     * Iridium gradients/rainbows become MiniMessage tags, all hex variants become {@code &#RRGGBB}.
     */
    static String normalize(String raw) {
        // MiniMessage refuses the section sign, so PlaceholderAPI output is folded into the '&' syntax first.
        String text = raw.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR);
        text = replaceAll(IRIDIUM_GRADIENT, text, m -> "<gradient:#" + m.group(1) + ":#" + m.group(3) + ">" + m.group(2) + "</gradient>");
        text = replaceAll(IRIDIUM_RAINBOW, text, m -> "<rainbow>" + m.group(2) + "</rainbow>");
        text = replaceAll(BRACE_HEX, text, m -> "&#" + m.group(1));
        text = replaceAll(BARE_HEX, text, m -> "&#" + m.group(1));
        return text;
    }

    private static String replaceAll(Pattern pattern, String text, java.util.function.Function<Matcher, String> replacer) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return text;
        StringBuilder builder = new StringBuilder();
        do {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacer.apply(matcher)));
        } while (matcher.find());
        matcher.appendTail(builder);
        return builder.toString();
    }

    /**
     * Walks the component tree and converts legacy codes inside text nodes.
     * The MiniMessage style of the node is kept as the base style of the converted text.
     */
    private static Component applyLegacy(Component component) {
        Component result = component;
        if (component instanceof TextComponent text && LEGACY_CODE.matcher(text.content()).find()) {
            Component converted = LEGACY_AMPERSAND.deserialize(text.content());
            List<Component> children = new ArrayList<>(text.children().size() + 1);
            children.add(converted);
            children.addAll(text.children());
            result = Component.text().style(text.style()).append(children).build();
        }
        List<Component> children = result.children();
        if (children.isEmpty()) return result;
        List<Component> mapped = new ArrayList<>(children.size());
        boolean changed = false;
        for (Component child : children) {
            Component mappedChild = applyLegacy(child);
            if (mappedChild != child) changed = true;
            mapped.add(mappedChild);
        }
        return changed ? result.children(mapped) : result;
    }

}
