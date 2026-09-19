# Menhir

Paper için kırılabilir liderlik taşları — dahili hologramlar, AFK koruması ve PlaceholderAPI desteği ile.
Hologram eklentisi gerekmez.

[![Build](https://github.com/musbabaff/Menhir/actions/workflows/build.yml/badge.svg)](https://github.com/musbabaff/Menhir/actions/workflows/build.yml)
[![License: MPL-2.0](https://img.shields.io/badge/license-MPL--2.0-blue.svg)](LICENSE)
[![Paper 1.21.4](https://img.shields.io/badge/Paper-1.21.4-9cf.svg)](https://papermc.io/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)

🇬🇧 [English README](README.md)

<p align="center">
  <img src="docs/images/hologram.png" alt="Hologramıyla bir Menhir taşı (ekran görüntüsü yer tutucusu)" width="600">
</p>

## Nedir

Menhir, dünyanızın istediğiniz yerine özel bloklar ("taşlar") koymanızı sağlar. Her taşın bir can değeri
vardır; bir oyuncu taşa her vurduğunda can bir azalır, oyuncunun vuruş sayısı bir artar. Can sıfıra
inince taş "kırılır": en çok vuran oyuncular ödül alır, blok bir bekleme süresi boyunca yer tutucu bir
materyale (örneğin bedrock) dönüşür ve sonra geri gelir. Taşın üzerindeki canlı hologram en iyi
oyuncuları, kalan canı ve yeniden doğma sayacını gösterir.

Eklenti, [MineBlocks](https://github.com/RAIXOCZ/MineBlocks) projesinin bir çatallamasıdır (fork).
Yapılandırma biçimini ve oyun içi düzenleyiciyi korur, ancak tüm dış bağımlılıkları kaldırır:
hologramlar Paper'ın kendi `TextDisplay` varlıklarıyla çizilir, AFK tespiti dahilidir ve her şey
Paper 1.21.4 / Java 21 üzerinde çalışır. Mevcut bir MineBlocks `config.yml` dosyası değişiklik
yapılmadan çalışmaya devam eder.

## Özellikler

* **Kırılabilir taşlar** — can, oyuncu başına vuruş sayısı, ilk 10 liderlik listesi, yer tutucu
  materyalle bekleme süresi, hareketsizlikte veya yeniden başlatmada otomatik sıfırlama, taş başına izin.
* **Dış eklenti olmadan canlı hologramlar** — `TextDisplay` tabanlı; yalnızca metin değiştiğinde ve
  yakında bir oyuncu varken güncellenir; taş başına ölçek, billboard modu, arka plan, gölge, hizalama,
  görüş mesafesi ve güncelleme aralığı; `#ICON: <materyal>` satırları dönen bir eşya modeline dönüşür.
* **Her renk sözdizimi** — MiniMessage gradyanları, eski `&` kodları, `&#RRGGBB` / `#RRGGBB` hex,
  karakter başına gradyanlar, PlaceholderAPI yer tutucuları (ItemsAdder yazı tipi simgeleri dahil).
* **Dahili AFK koruması** — *n* saniyedir hareket etmeyen oyuncular taşa vuramaz; yalnızca gerçek konum
  değişikliği sayılır, etrafa bakmak sayılmaz.
* **Sıraya göre ödüller** — 1., 1.–3. … oyuncular için, her *n*. vuruş için, son vuruş için, vuruş
  aralığı için komutlar; rastgele ("şans;komut") veya tüm komutlar modu; çevrimdışı ödüller saklanır ve
  girişte verilir.
* **PlaceholderAPI genişletmesi** `menhir` — can, yüzde, durum, sayaç, taş başına ve genel sıralamalar,
  bakan oyuncunun kendi vuruşu ve sırası, en yakın yeniden doğma.
* **Hologram şablonları** — hologramı bir kez tanımlayın, her taşta kullanın.
* **Oyun içi düzenleyici** — `/menhir edit <taş>` konum, tür, can, hologram, bekleme süresi, ödüller,
  alet filtreleri ve sıfırlama seçenekleri için bir arayüz açar.

## Gereksinimler

| | |
|---|---|
| Sunucu | [Paper](https://papermc.io/) 1.21.4 veya üstü (Paper API zorunludur; Spigot desteklenmez) |
| Java | 21 |
| İsteğe bağlı | [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) (hologram/komutlarda yer tutucular ve `menhir` genişletmesi), [ItemsAdder](https://itemsadder.devs.beer/) (PlaceholderAPI üzerinden yazı tipi simgeleri), [Vault](https://www.spigotmc.org/resources/34315/) (sıralamada oyuncu ön ekleri) |

## Kurulum

1. [Sürümler sayfasından](https://github.com/musbabaff/Menhir/releases) `Menhir-<sürüm>.jar` dosyasını
   indirip `plugins/` klasörüne koyun.
2. Sunucuyu bir kez başlatın. `plugins/Menhir/config.yml` oluşturulur (bir `plugins/MineBlocks/` klasörü
   varsa `config.yml` ve `storage/` otomatik olarak kopyalanır).
3. Taşın olacağı yerde durup `/menhir create <ad>` ve ardından `/menhir edit <ad>` çalıştırın — ya da
   `config.yml` dosyasını düzenleyip `/menhir reload` yazın.

## Yapılandırma

Tam varsayılan dosya: [`src/main/resources/config.yml`](src/main/resources/config.yml); eski biçimde
kısa bir örnek: [`examples/escraft-config.yml`](examples/escraft-config.yml).

### `lang`

| Anahtar | Açıklama | Örnek |
|---|---|---|
| `top.nobody` | Boş sıralama satırında gösterilen ad | `"&cKimse"` |
| `top.nobody-breaks` | Boş satırda gösterilen vuruş sayısı | `"0"` |
| `top.not_ranked` | İlk 10 dışındaki oyuncu için `%menhir_<taş>_my_rank%` | `"Sırasız"` |
| `timeout.message` | Sayaç metni; `%time%` kalan süreyle değiştirilir | `"&cYenilenme: %time%"` |
| `timeout.max-units` | Kaç birim gösterilir (`1 saat 5 dakika` → 2) | `2` |
| `timeout.units.{hour,hours,minute,minutes,second,seconds}` | Birim son ekleri | `" dk"` |
| `status.timeout` | Kırık taşa vurulunca gösterilir | |
| `status.afk` | AFK oyuncu taşa vurunca gösterilir | |
| `status.no-permission` | Oyuncunun taşın izni yoksa gösterilir | |
| `status.invalid-tool` | Alet izinli değilse gösterilir | |

### `options`

| Anahtar | Açıklama | Varsayılan |
|---|---|---|
| `afk.enabled` | AFK korumasını aç | `true` |
| `afk.seconds` | Kaç saniye hareketsizlikten sonra oyuncu AFK sayılır | `15` |
| `afk.notification-type` | AFK mesajı için `ACTIONBAR`, `CHAT`, `TITLE` veya `NONE` | `ACTIONBAR` |
| `notification-type` | Diğer durum mesajları için aynı seçenekler | `ACTIONBAR` |
| `block-break-limit` | Bir oyuncunun iki vuruşu arasındaki en az milisaniye (taş başına `break-limit` geçersiz kılar) | `20` |
| `offline-rewards` | Çevrimdışı oyuncuların ödüllerini sakla ve girişte çalıştır | `true` |
| `hologram.*` | Genel hologram varsayılanları, aşağıya bakın | |
| `afk-integration-enabled`, `hologram-update-interval` | **Eski** MineBlocks anahtarları — hâlâ okunur, konsola bir kez uyarı yazılır | |

### Hologram ayarları (`options.hologram`, `hologram-templates.<ad>`, `blocks.<id>.hologram`)

Ayarlar katmanlıdır: dahili varsayılanlar ← `options.hologram` ← şablon ← taş. Taşta yazılan anahtar
şablonu, şablon da genel varsayılanları geçersiz kılar.

| Anahtar | Açıklama | Varsayılan |
|---|---|---|
| `offset.{x,y,z}` | Hologramın **üst** noktasının blok merkezi + 1,5 bloğa göre kayması (MineBlocks ile aynı anlam) | `0` |
| `lines` | Satır listesi; yer tutuculardan sonra boş kalan satır hiç gösterilmez | |
| `template` | Bir `hologram-templates` girdisinin adı (yalnızca taşta). `default` otomatik kullanılır | |
| `scale` | Metin boyutu çarpanı | `1.0` |
| `billboard` | `CENTER`, `VERTICAL`, `HORIZONTAL`, `FIXED` | `CENTER` |
| `background` | `transparent`, `default`, `#RRGGBB` veya `#AARRGGBB` | `transparent` |
| `text-shadow` | Metin arkasında gölge | `false` |
| `see-through` | Blokların arkasından görünür | `false` |
| `alignment` | `CENTER`, `LEFT`, `RIGHT` | `CENTER` |
| `view-distance` | Blok; bu kadar yakında kimse yokken güncelleme gönderilmez | `48` |
| `update-interval` | Yeniden çizimler arası saniye; `-1` = her tick (metin yalnızca değişince gönderilir) | `-1` |
| `line-width` | Satır kaydırmadan önceki piksel genişliği | `1000` |

Özel satır: `#ICON: %type%` (veya `#ICON: DIAMOND_BLOCK`) metnin üstünde o materyalin yavaşça dönen
modelini gösterir. Tanınmayan materyaller konsola bir kez yazılır ve atlanır.

Aynı satırda kullanılabilen metin biçimleri:

| Sözdizimi | Örnek |
|---|---|
| Eski kodlar | `&7Top &8- &a%health%` |
| Hex | `&#2C74B3Metin`, `{#2C74B3}Metin`, `#2C74B3Metin` |
| Karakter başına gradyan | `&#FF0000M&#FF1100e&#FF2200n` |
| MiniMessage | `<gradient:#FF4500:#FFD700><b>MENHIR</b></gradient>` |
| MineBlocks gradyanı | `<#FF4500>MENHIR</#FFD700>` |
| PlaceholderAPI | `%img_general_icon_16%`, `%server_online%` |

İşleme sırası: önce MiniMessage, sonra eski/hex kodlar.

```yaml
hologram-templates:
  default:
    scale: 1.1
    lines:
      - '#ICON: %type%'
      - '<gradient:#FF4500:#FFD700><b>METİN TAŞI</b></gradient>'
      - '&7%player_1% &8- &e%player_1_breaks%'
      - '&7%player_2% &8- &e%player_2_breaks%'
      - '&a%health%&7/&a%max_health%'
      - '&c%timeout%'

blocks:
  kuzey:
    hologram:
      template: default   # isteğe bağlı, "default" otomatik seçilir
      offset: {y: 1.2}
      billboard: VERTICAL # şablonu geçersiz kılar
```

### `blocks.<id>`

| Anahtar | Açıklama |
|---|---|
| `location.{world,x,y,z}` | Blok konumu |
| `type` | Taşın materyali (`GOLD_BLOCK`, `DEEPSLATE_BRICKS`, …) |
| `health` | Kırmak için gereken vuruş sayısı |
| `permission` | Vurmak için gereken izin (`""` = yok) |
| `break-limit` | Bir oyuncunun iki vuruşu arası milisaniye; `-1` = `options.block-break-limit` |
| `hologram.*` | Yukarıya bakın |
| `afk.{enabled,seconds,notification-type}` | Taş başına AFK ayarı (isteğe bağlı) |
| `timeout.time` | Kırıldıktan sonraki bekleme süresi, saniye (`-1` = yok) |
| `timeout.type` | Bekleme süresinde gösterilen materyal (ör. `BEDROCK`) |
| `timeout.respawn` | Taş geri geldiğinde yayınlanan mesaj (metin veya liste) |
| `messages.break` | Taş kırıldığında yayınlanan mesaj (metin veya liste) |
| `reset.inactive.time` | Bu kadar saniye vuruş olmazsa can ve sayaçlar sıfırlanır (`-1` = asla) |
| `reset.inactive.message` | Bu sıfırlamada yayınlanan mesaj |
| `reset.onrestart` | Sunucu (yeniden) başlayınca can ve sayaçları sıfırla |
| `tool.types` | `"<materyal regex>: ALLOWED\|DENIED"` dizeleri listesi **veya** bir harita; `default` varsayılanı belirler |
| `tool.enchantments.default` | Büyüler için varsayılan |
| `tool.enchantments.<ad>.{level,type}` | `level` sayı veya aralık (`4-5`), `type` `ALLOWED`/`DENIED` |
| `tool.names.default`, `tool.names.<görünen ad>` | Eşya adı filtresi (görünen metin karşılaştırılır) |
| `rewards.<ad>.*` | Aşağıya bakın |

### Ödüller

```yaml
rewards:
  birinci:
    type: top
    place: 1            # ya da aralık: 1-3
    mode: random        # random = şansa göre seç (varsayılan) | all = tüm komutları çalıştır
    commands:
      - "10;give %player% diamond"      # şans;komut
      - "100;give %player% iron_ingot"
  her_besinci:
    type: break
    interval: 5         # oyuncunun her 5. vuruşunda
    commands: ["100;give %player% iron_nugget"]
  son_vurus:
    type: break
    condition: last     # ayrıca: "less than 5", "more than 10", "equal to 3", "5-10"
    commands: ["100;give %player% netherite_scrap"]
  emekci:
    type: break_count   # taş kırıldığında oyuncunun bu taştaki toplam vuruşu
    from: 10
    to: 50
    commands: ["100;give %player% emerald"]
```

Komutlar konsol tarafından çalıştırılır. Komut ve mesajlardaki yer tutucular: `%player%`, `%uuid%`,
`%breaks%` ve tüm PlaceholderAPI yer tutucuları.

## Yer tutucular

### Hologram satırlarında

| Yer tutucu | Değer |
|---|---|
| `%health%` / `%max_health%` | Mevcut / en yüksek can |
| `%type%` | Taşın materyal adı |
| `%timeout%` | Biçimlendirilmiş kalan bekleme süresi; taş sağlamken boştur (ve satır gizlenir) |
| `%player_1%` … `%player_10%` | *n*. oyuncunun adı (veya `lang.top.nobody`) |
| `%player_1_breaks%` … `%player_10_breaks%` | *n*. oyuncunun vuruşu (veya `lang.top.nobody-breaks`) |
| `%player_1_prefix%` … | *n*. oyuncunun Vault sohbet ön eki (Vault yoksa boş) |
| herhangi bir `%papi_yer_tutucusu%` | Her güncellemede PlaceholderAPI ile çözülür |

### PlaceholderAPI genişletmesi `menhir`

| Yer tutucu | Değer |
|---|---|
| `%menhir_<taş>_health%` | Mevcut can |
| `%menhir_<taş>_max_health%` | En yüksek can |
| `%menhir_<taş>_percent%` | Yüzde olarak can (0–100) |
| `%menhir_<taş>_status%` | `alive` veya `broken` |
| `%menhir_<taş>_timeout%` | Biçimlendirilmiş kalan süre, sağlamken boş |
| `%menhir_<taş>_top_<n>_name%` | *n*. oyuncunun adı |
| `%menhir_<taş>_top_<n>_breaks%` | *n*. oyuncunun vuruşu |
| `%menhir_<taş>_my_breaks%` | Bakan oyuncunun bu taştaki vuruşu |
| `%menhir_<taş>_my_rank%` | Bakan oyuncunun sırası (veya `lang.top.not_ranked`) |
| `%menhir_global_top_<n>_name%` | Tüm taşların toplamına göre *n*. oyuncunun adı |
| `%menhir_global_top_<n>_breaks%` | Toplam vuruşu |
| `%menhir_next_respawn%` | En yakın yeniden doğmaya kalan süre, kırık taş yoksa boş |

MineBlocks adları (`%mb_<taş>_hp%`, `_max_hp`, `_breaks`, `_rank`, `_top_<n>`, `_top_breaks_<n>`) `menhir`
tanımlayıcısıyla da kabul edilir, ör. `%menhir_kuzey_hp%`.

## Komutlar ve izinler

Ana komut: `/menhir` (takma adlar `/metin`, `/menhirstone`, `/mb`).

| Komut | Açıklama | İzin |
|---|---|---|
| `/menhir help` | Komut listesi | `menhir.help` |
| `/menhir reload` | `config.yml` ve tüm taşları yeniden yükle | `menhir.reload` |
| `/menhir list` | Taşları ve konumlarını listele | `menhir.list` |
| `/menhir create <id>` | Bulunduğun yerde taş oluştur | `menhir.create` |
| `/menhir edit <id>` | Düzenleme arayüzünü aç | `menhir.edit` |
| `/menhir remove <id>` | Taşı sil (yalnızca konsol; oyunda düzenleyiciyi kullan) | `menhir.remove` |
| `/menhir teleport <id>` | Taşa ışınlan | `menhir.teleport` |
| `/menhir reset <id>` | Can ve sayaçları sıfırla | `menhir.reset` |
| `/menhir sethealth <id> <n>` | Mevcut canı ayarla | `menhir.sethealth` |
| `/menhir hologram show <id>` | Hologram satırlarını düzenleme bağlantılarıyla göster | `menhir.hologram` |
| `/menhir hologram addline <id> <metin>` | Satır ekle | `menhir.hologram` |
| `/menhir hologram setline <id> <n> <metin>` | *n*. satırı değiştir | `menhir.hologram` |
| `/menhir hologram removeline <id> <n>` | *n*. satırı sil | `menhir.hologram` |
| `/menhir version` | Kurulu sürüm | `menhir.version` |
| `/menhir wiki` | Bu sayfanın bağlantısı | — |

Her alt komut için `menhir.admin` (varsayılan: OP) gerekir; bu izin tüm `menhir.*` izinlerini içerir.

## MineBlocks'tan geçiş

* `Menhir-<sürüm>.jar` dosyasını koyun, `MineBlocks-*.jar` dosyasını kaldırın. İlk açılışta Menhir
  `plugins/MineBlocks/config.yml` ve `storage/` klasörünü `plugins/Menhir/` içine **kopyalar**; eski klasöre
  dokunulmaz.
* `config.yml` dosyanız değişmeden çalışır: tüm `lang`, `options`, `blocks.<id>.*`, `tool`, `rewards`
  anahtarları aynı anlamla okunur. `options.afk-integration-enabled`, `options.notification-type` ve
  `options.hologram-update-interval` hâlâ okunur (yeni anahtarlar için bir kez uyarı yazılır).
* DecentHolograms, CMI, Essentials, AFKPlus, UltraAFK ve LuckPerms artık gerekmez. Hologramları Menhir
  kendisi çizer; eski `mineblock-<id>` adlı DecentHolograms hologramları `/dh delete mineblock-<id>` ile
  (veya DecentHolograms'ı kaldırarak) silinebilir.
* `#ICON: %type%` satırları çalışmaya devam eder (dönen eşya modeli olarak).
* `/mb` komutu takma ad olarak çalışır; izinler `mb.admin.*` yerine `menhir.*` oldu
  (`mb.admin` → `menhir.admin`).
* PlaceholderAPI tanımlayıcısı değişti: `%mb_…%` → `%menhir_…%` (eski parametre adları da çalışır).
* Oyuncu ön ekleri artık Vault'tan gelir: `%player_<n>_prefix%` kullanıyorsanız Vault kurun.
* Taş verileri (`storage/<id>.mb`) aynı biçimdedir; hiçbir şey kaybolmaz.
* Paper 1.21.4 ve Java 21 gerekir — Spigot ve eski sürümler desteklenmez.

## SSS

**DecentHolograms / CMI gerekli mi?**
Hayır. Menhir hologramları Paper'ın `TextDisplay` varlıklarıyla çizer. Başka eklenti gerekmez.

**Hologram görünmüyor / yanlış yükseklikte duruyor.**
`hologram.offset.y` değerini ayarlayın. Kayma, MineBlocks'taki gibi hologramın *üst* noktasından ölçülür;
mevcut değerler aynı anlamı korur. Chunk'ın yüklü olduğundan ve `view-distance` içinde olduğunuzdan emin olun.

**Çökme sonrası artık hologram varlıkları kaldı mı?**
Menhir varlıklarını `menhir_hologram` anahtarıyla işaretler ve bilinmeyenleri her chunk yüklenişinde,
`/menhir reload` ile ve kapanışta siler. Varlıklar kalıcı değildir, dünyaya hiç yazılmaz.

**Oyuncu etrafa bakıyor ama neden "AFK" sayılıyor?**
Yalnızca konum değişikliği sayılır; kamerayı çevirmek hareket değildir. `options.afk.seconds` değerini
düşürün veya kontrolü istemiyorsanız `options.afk.enabled: false` yapın.

**Hologram satırlarında ItemsAdder simgeleri kullanabilir miyim?**
Evet — PlaceholderAPI kurun ve ItemsAdder yazı tipi yer tutucularını (`%img_…%`) kullanın. Her hologram
güncellemesinde çözülürler.

**Spigot / 1.20 üzerinde çalışır mı?**
Hayır. Hologram motoru Paper'ın Adventure ve Display varlık API'lerini gerektirir (1.21.4+).

**Bir hologram düzenini taşlar arasında nasıl paylaşırım?**
`hologram-templates.default.lines` içine koyun ve taşlardan `lines` anahtarını silin; taş başına yalnızca
`offset` bırakın. Kendi `lines` anahtarı olan taş onu kullanır.

**Taşların AFK eşikleri farklı olabilir mi?**
Evet: `blocks.<id>.afk.seconds: 30` (ayrıca `enabled`, `notification-type`).

## Teşekkür

Menhir, **RAIXOCZ** tarafından MPL-2.0 lisansıyla yayımlanan
[MineBlocks](https://github.com/RAIXOCZ/MineBlocks) projesinin bir çatallamasıdır. Blok modeli, ödül
sistemi, alet filtreleri ve oyun içi düzenleyici o projeden gelir; atıf ayrıntıları ve değişikliklerin tam
listesi için [NOTICE.md](NOTICE.md) dosyasına bakın.

Gömülü kütüphaneler: [ACF](https://github.com/aikar/commands) (MIT), [MineDown](https://github.com/Phoenix616/MineDown) (MIT).

## Lisans

[Mozilla Public License 2.0](LICENSE). Değişiklikler © 2026 musbabaff.
