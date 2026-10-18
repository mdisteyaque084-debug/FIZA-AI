# Fiza AI — Stage 1 + authorized screen actions

This is an Android/Kotlin + Jetpack Compose project. The updated build adds an opt-in Android Accessibility Service for semantic tap, scroll, back/home, and text-field actions, plus a review-before-opening WhatsApp draft flow.

## Build on Android Studio (phone + Chromebook)

1. Install Android Studio (recent stable version) and its Android SDK Platform 34; use the bundled JDK 17.
2. Extract this folder and open the `FizaAI` directory (the one containing `settings.gradle.kts`).
3. Let Android Studio sync Gradle while connected to the internet. This archive does not include the Gradle wrapper, so use Android Studio's Gradle sync/build controls or generate a wrapper from a local Gradle installation.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**. If build succeeds, Android Studio shows the APK path under `app/build/outputs/apk/debug/`.
5. Install on an Android phone using Run/USB debugging or copy the APK to the device and install it. On a Chromebook, this works only on models that support Android apps and allow sideloading/developer installation; Chromebook compatibility has not been tested here.

## First-run setup

1. Open Fiza AI and grant microphone permission when prompted.
2. If Hindi speech output is unavailable, install a Hindi voice in Android Settings > Text-to-speech.
3. For screen control, tap **Screen control permission setup**. Android opens Accessibility settings. Read the system disclosure and enable **Fiza AI screen control** only if you want these actions.
4. Accessibility is optional. You can continue to use voice recognition and app launching without enabling it.

## Supported voice command examples

- `Fiza, WhatsApp kholo`
- `YouTube kholo aur cricket search karo`
- `neeche scroll karo` / `upar scroll karo`
- `pichhle page par jao` / `back jao`
- `tap Search` / `Search par tap karo`
- `type hello there` (types into an accessible editable field)
- `text clear` (clears the first focused/editable field)
- `Rahul ko message likho main ghar pahunch gaya hoon`

## WhatsApp draft safety

Fiza shows the draft in a review dialog first. Selecting **WhatsApp mein draft kholen** opens a `wa.me` text-draft URL; the recipient is not assumed or auto-selected. You must choose and verify the correct recipient yourself, review the content in WhatsApp, and tap Send manually. This project does **not** automatically send messages and does not resolve ambiguous contacts.

## Accessibility limitations and privacy

- Screen actions work only when the target app exposes accessible nodes and the service is enabled. Some apps, custom canvases, secure screens, or protected fields may not expose useful controls.
- Taps match accessible text/content descriptions; if a label is ambiguous or absent, Fiza should not be expected to find it reliably. The current implementation does not use coordinate gestures.
- Fiza does not unlock the phone, bypass authentication, secretly monitor the screen, or send messages automatically.
- `TYPE_TEXT` and `CLEAR_TEXT` act on the first focused/editable accessible field. Check the field before dictating private content.
- App launching reports whether Android accepted the intent, not proof that the app finished loading.
- There is no AI backend in this project yet; these are local command rules, Android speech recognition, and Android TextToSpeech.

## Test checklist (requires an Android device/emulator)

- [ ] Grant microphone permission and confirm voice recognition returns a transcript.
- [ ] Open WhatsApp, Settings, YouTube, Camera and Gallery where installed/available.
- [ ] Enable Accessibility service and test scroll down/up on a normal scrollable app.
- [ ] Test `tap Search` on a screen with a clearly labelled accessible Search control.
- [ ] Focus a harmless text field and test `type hello there`, then `text clear`.
- [ ] Say a message draft command; confirm the dialog shows the exact message and that Cancel does not open WhatsApp.
- [ ] Choose Open Draft; confirm no message is sent automatically and the user must choose a recipient and tap Send.
- [ ] Disable Accessibility service and confirm screen actions report that setup is needed rather than claiming success.

## Verification status

The ZIP was inspected and the source/manifest/resources were updated. This environment does not have Gradle, an Android SDK, or a connected Android device, so compilation, APK generation, and on-device behavior have **not** been verified here. Build and run the checklist above in Android Studio before relying on it.


## Premium UI update
The main screen has a premium dark violet/pink theme, animated assistant orb, clearer listening/thinking/speaking status, transcript/reply panel, recent commands, and cloud build workflow at `.github/workflows/android-build.yml`.

## Important capability note
This project still uses local command parsing and Android speech recognition/TextToSpeech. It does not yet include a ChatGPT-style AI backend. Add a secure backend/API integration to provide open-ended AI conversation; do not embed secret API keys in the APK. Android accessibility actions require the user to explicitly enable the service and are limited by what other apps expose.

## Cloud build
Upload the contents of this `FizaAI` directory (including the hidden `.github` folder) to the root of a GitHub repository. Open Actions and run “Build Fiza AI APK”. Download the `FizaAI-debug-apk` artifact if the workflow succeeds. This source has not been compiled or device-tested in this environment.
