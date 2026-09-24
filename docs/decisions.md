
# LensBridge — Build Log

Running log of real decisions and problems hit while building.
Write entries as they happen, not reconstructed after.

---

## 2026-09-19 — PlatformView bridge: first render + IDE setup pain

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

**Why it matters:** The PlatformView contract itself (matching view
type strings between Dart and Kotlin) worked first try — the real
friction was tooling, not the bridge. Readers doing this hands-on will
likely hit the same SDK-linking wall.

---

## 2026-09-20 — Camera permission: manifest declaration isn't enough

**Context:** Camera preview rendered in CameraX logs (stream configured,
surface connected) but nothing showed on screen — expected a system
permission dialog on launch since CAMERA was declared in the manifest.

**What happened:** Declaring `<uses-permission>` in AndroidManifest.xml
does NOT trigger a runtime prompt on Android 6.0+ — it only declares
intent to use the permission. Without an explicit runtime request,
the permission stays denied by default, silently. No crash, no
visible error — just a working pipeline with no feed.

Tried to shortcut-confirm via `adb shell pm grant ...` but hit a
SecurityException — my physical device's OEM build (Oplus/OnePlus-
based) restricts shell-level permission grants that stock Android
adb normally allows.

**Decision / fix:** Added explicit runtime permission request via
`ActivityCompat.requestPermissions()` in MainActivity — this is the
actual correct fix regardless of the adb detour, since real users
need this flow too, not just my dev device.

**Why it matters:** "It's in the manifest" is a common wrong mental
model coming from iOS, where Info.plist entries DO trigger automatic
prompts. Android's dangerous-permission model requires explicit
runtime code.

**Still open:** no handling yet for permission denial, "don't ask
again," or a re-check before the view tries to bind. Deferred, not
forgotten.

---

## 2026-09-22 — Stop doesn't blank the preview (expected, not a bug)

**Context:** Hitting Stop after wiring MethodChannel controls left the
last camera frame frozen on screen instead of going blank.

**What happened:** `PreviewView` is backed by a SurfaceView/TextureView.
`unbindAll()` stops CameraX from pushing new frames, but doesn't clear
the surface's existing buffer — so the last rendered frame just stays
displayed. Confirmed genuinely stopped (not stalled) by checking that
the frame didn't update when waving a hand in front of the camera.

**Decision / fix:** Added explicit `previewView.visibility` toggling
(INVISIBLE on stop, VISIBLE on start/bind) so "Stop" reads as visually
stopped rather than frozen. Centralized the VISIBLE toggle inside
`bindPreview()` so start and lens-switch paths don't duplicate it.

**Why it matters:** A legitimate CameraX framework behavior that reads
as a bug to anyone testing casually. Good concrete gotcha for the post.

---

## 2026-09-22 — Overlay invisible despite working detection pipeline

**Context:** ML Kit face detection confirmed working (logs showed
face_count=1), but no green box appeared on screen.

**What happened:** OverlayView was added to the container FrameLayout
via addView(view) with no explicit LayoutParams. Default is
WRAP_CONTENT, and since OverlayView has no intrinsic content (just a
custom View doing manual canvas drawing), it measured to 0x0 — onDraw
was still being called, just onto a canvas with no area.

**Decision / fix:** Explicit MATCH_PARENT LayoutParams on both
previewView and overlayView when adding to the container.

**Why it matters:** A completely silent failure mode — no crash, no
log, pipeline genuinely working end-to-end, just literally nothing to
look at. Worth flagging in the post since it's a very easy trap when
stacking a custom-drawn View over a PreviewView inside a container.

---

## 2026-09-24 — Two more ML Kit / ImageAnalysis gotchas (same session)

**Context:** Wiring `ImageAnalysis` + ML Kit face detection alongside
the existing `Preview` use case.

**What happened / decisions:**
- **`imageProxy.close()` must be called on every frame**, success or
  failure, or CameraX stalls the analysis pipeline after a few frames
  (it assumes the previous frame is still being processed). Put the
  close call in `addOnCompleteListener` rather than
  `addOnSuccessListener`, since complete fires regardless of outcome.
- **ML Kit face coordinates are in sensor space, not display space** —
  on a phone held in portrait these are rotated 90° relative to each
  other, so width/height get swapped between the two. `OverlayView`'s
  scaleX/scaleY intentionally divide by the swapped dimension
  (`imageHeight` for scaleX, `imageWidth` for scaleY) to compensate.

**Why it matters:** Both are well-known but easy-to-miss gotchas in
CameraX + ML Kit integrations — exactly the kind of detail that
separates "I copied a tutorial" from "I understand what's happening
at the frame level."

---

## 2026-09-24 — Transient stream config warning during lens switch

**Context:** Logcat showed
`Stream configuration failed due to: endConfigure:679: Camera 0:
Unsupported set of inputs/outputs provided` during a back→front lens
switch triggered via the Switch Lens button.

**What happened:** Appears to be a transient race from unbinding the
back camera's session and immediately rebinding to the front camera —
the front camera opened successfully right after and face detection
resumed normally. Not yet confirmed as fully harmless; flagging as an
open watch item rather than a resolved one.

**Still open:** Keep an eye on this if lens switching ever feels flaky
or drops a frame visibly. Possible future fix: add a short delay or
explicit await between unbind and rebind if it turns out to matter.