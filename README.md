# Uchoq — Android messenjer

**Uchoq** — Telegram platformasi asosida yaratilayotgan norasmiy Android messenjer. Loyiha tezkorlik, qulaylik va zamonaviy interfeysga e’tibor qaratadi.

Ushbu repozitoriy Uchoq Android ilovasining ochiq manba kodini o‘z ichiga oladi. Loyiha Telegram for Android manba kodi asosida ishlab chiqilmoqda.

## Telegram API

Uchoq Telegram API va MTProto protokolidan foydalanadi. Telegram talablariga muvofiq, ilova uchun alohida `api_id` va `api_hash` olish kerak.

1. O‘z `api_id` va `api_hash` ma’lumotlaringizni [Telegram API sahifasi](https://core.telegram.org/api/obtaining_api_id) orqali oling.
2. Uchoq Telegram’ning rasmiy ilovasi emas.
3. Foydalanuvchilarning ma’lumotlari va maxfiyligini himoya qilish uchun [Telegram xavfsizlik tavsiyalari](https://core.telegram.org/mtproto/security_guidelines)ga amal qiling.
4. Loyiha litsenziyasi talablariga rioya qilgan holda o‘zgartirilgan manba kodini ochiq saqlang.

### API va protokol hujjatlari

- [Telegram API hujjatlari](https://core.telegram.org/api)
- [MTProto protokoli](https://core.telegram.org/mtproto)

## Loyihani build qilish

> **Muhim:** Asl Telegram repozitoriysi reproducible build uchun test `release.keystore`, `google-services.json` va `BuildVars.java` ichida namunaviy qiymatlarni o‘z ichiga oladi. Uchoq’ning APK/AAB faylini tarqatishdan oldin ularni o‘zingizning kalit va konfiguratsiyalaringiz bilan almashtiring.

Kerakli vositalar:

- Android Studio 2025.1.4
- Android NDK 27.2.12479018
- Android SDK 36

### O‘rnatish va sozlash

1. Repozitoriyni submodule’lari bilan clone qiling:
   ```bash
   git clone --recursive --shallow-submodules https://github.com/javaoff364-cloud/Uchoq.git Uchoq
   ```
2. Agar submodule’lar olinmagan bo‘lsa:
   ```bash
   git submodule init
   git submodule update --init --recursive --depth=1
   ```
3. O‘zingizning `release.keystore` faylingizni `TMessagesProj/config` ichiga joylashtiring.
4. `gradle.properties` ichida release signing ma’lumotlarini sozlang.
5. Firebase loyihasini Uchoq package ID’lari uchun sozlang va `google-services.json` faylini kerakli joyga qo‘ying.
6. Loyihani Android Studio’da **Open** orqali oching.
7. `BuildVars.java` ichida o‘zingizning Telegram API va boshqa xizmat kalitlaringizni kiriting.
8. Shundan so‘ng Uchoq’ni build qilishingiz mumkin.

## Uchoq konfiguratsiyasi

Asosiy Android application ID:

```
uz.uchoq.app
```

Debug build odatda `.beta` suffix bilan yaratiladi.

## Litsenziya

Uchoq Telegram for Android manba kodi asosida yaratilgan va GNU GPL v2 yoki undan keyingi versiya shartlari asosida tarqatiladi. Asl mualliflarning copyright va litsenziya bildirishnomalari saqlanadi.

Telegram — Telegram Messenger LLP va tegishli huquq egalarining savdo belgisi. Uchoq Telegram tomonidan tasdiqlangan yoki rasmiy Telegram ilovasi emas.
