## 2026-09-22 — PlatformView bridge: first render + IDE setup pain

**Context:** Wiring up the first PlatformViewFactory + placeholder native
View, just to prove Dart <-> Kotlin actually connects before touching
CameraX.

**What happened:** Kotlin code wouldn't compile in Android Studio —
"Unresolved reference 'android'" on every import, even kotlin.Any
itself. Flutter build succeeded and the app ran fine (green box showed
up), but the native code was uneditable — no autocomplete, no error
checking. Root cause: the project had no SDK linked at the IDE level
(Project Structure showed <No SDK>), and separately, Android Studio
hadn't linked the nested Gradle module. Neither issue was in my code —
both were IDE/project state.

**Decision / fix:**
- Found the actual compileSdk value by reading the hex dump in
  `flutter run --verbose` output (0x24 = API 36) rather than guessing
- Set that SDK explicitly in Project Structure
- Used "Link Gradle project" (Android Studio's newer replacement for
  the classic sync button) pointed at android/build.gradle.kts
- Opened `android/` as its own Android Studio window, separate from
  the Flutter root in VS Code, rather than fighting for one unified
  window

