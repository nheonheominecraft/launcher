# Simple TV Launcher (Màn hình TV Dễ Dùng)

A home-screen launcher for Android TV / Google TV, made for elderly users.
Huge tiles, high contrast, D-pad only, Vietnamese by default. No ads, no tracking,
no account, no subscription.

## STATUS - read this first

**This source was written but NOT compiled or run.** The environment it was written in
had no Android SDK, no Gradle and no internet, so there is **no APK** and nothing has been
tested on a real TV or emulator. I did static checks only (XML is well-formed, every
`R.string` / `R.drawable` reference exists, English and Vietnamese strings match, brackets
balance). Expect to fix a few small compile errors on first build. Everything below
describes what the code is designed to do, not what has been verified on a device.

## Why plain Android Views instead of Compose for TV

The brief said to use Compose for TV if the dependencies are available. They could not be
checked offline, so I chose the framework `Views` toolkit with **zero dependencies**:
no version I could not verify, a smaller APK, less RAM, native D-pad focus handling, and
no library that could contain analytics. It is a deliberate trade-off, not a claim that
it is better in every way.

## Build

Requirements: Android Studio (recent) or JDK 17+ and Gradle 8.7, Android SDK platform 34.

1. Open this folder in Android Studio and let it sync (it downloads Gradle and the Android
   Gradle Plugin 8.5.2 / Kotlin 1.9.24). If Studio offers newer versions, accept.
   Command line instead: run `gradle wrapper --gradle-version 8.7` once, then use `./gradlew`.
2. Two variants (flavors):
   * `offline` (recommended) - **declares no permissions at all**, no weather.
   * `weather` - adds only `INTERNET`, plus an optional weather line (OFF until enabled).
3. Build:
   ```
   ./gradlew assembleOfflineRelease     # app/build/outputs/apk/offline/release/
   ./gradlew assembleOfflineDebug       # debug build
   ./gradlew assembleWeatherRelease     # variant with weather
   ```
   The release build is signed with the debug key so it can be sideloaded. Use your own
   keystore if you distribute it.
4. Install: `adb connect <tv-ip>` then `adb install -r app-offline-release.apk`.

## Make it the default home screen

The app declares the `HOME` intent filter (plus `LAUNCHER` and `LEANBACK_LAUNCHER`, so it
can always be opened from the apps list). Whether it can be the default depends on the TV:

1. Press HOME on the remote. Some Android TV devices show a chooser: pick this app, "Always".
2. Otherwise, from a computer:
   `adb shell cmd package set-home-activity vn.simpletv.launcher/.HomeActivity`
3. Settings inside the app shows green/orange status telling whether it really is the
   default (it asks Android, it does not guess).

**Known restrictions (not worked around, on purpose):**
* Many Google TV devices (e.g. Chromecast with Google TV) and some operator/manufacturer
  TVs ignore step 1 or force their own launcher. Step 2 works on some of them and not
  others. I do not recommend disabling the stock launcher package with `pm disable-user`:
  it can leave a TV without a working home screen.
* If it cannot be the default, it still works as a normal app opened from the apps row.
* "Google TV" tile = opens the stock home screen (found through the HOME intent). It is
  the way back to the original UI and is only shown if one exists.

## HDMI / TV input

The tile uses `TvInputManager` to list pass-through inputs and
`TvContract.buildChannelUriForPassthroughInput` to ask the system TV app to tune to one.
* Hidden when the device reports no pass-through inputs (typical for streaming sticks).
* If the system refuses the request, a clear message says to use INPUT / SOURCE on the remote.
* Manufacturer-only input switching APIs are NOT used. Untested on real hardware.

## Features

* Home: large clock + date, tiles for YouTube, YouTube TV, Netflix, Google TV (stock home),
  TV/HDMI, All apps, Settings. Only installed / supported things are shown.
* D-pad only: yellow filled tile + white frame shows focus; OK opens; BACK never exits
  the home screen (jumps to the first tile); HOME always returns (singleTask).
* Text size: Normal / Large / Extra large (also changes the number of columns).
* Simple mode: first 4 apps, 2x2 giant tiles, one small Settings button for family.
* Choose apps / Arrange apps (pick up with OK, move with UP/DOWN, drop with OK).
* Lock layout (ON by default): choose/arrange are blocked until it is switched off.
  There is no long-press editing anywhere on the home screen.
* Language: Vietnamese (default) and English; "same as TV".
* Settings is one flat list with a plain description under every item.
* Every screen has a "Back to main screen" button at the bottom.
* Accessibility: all items are focusable; labels are real text (TalkBack-readable).

New apps installed later are not added to the home screen automatically (that avoids
surprise changes); a family member adds them in Settings > Choose apps.

## Privacy

* No analytics, ads, crash reporters, accounts or third-party code (`dependencies {}` is empty).
* Settings are stored only in the app's private storage; `allowBackup="false"`.
* `offline` flavor: no `INTERNET` permission, so it physically cannot connect.
* `weather` flavor: when enabled, sends the latitude/longitude of the chosen *city centre*
  (from a fixed list) to `api.open-meteo.com` at most every 30 min. The server naturally
  sees the TV's IP address. Check Open-Meteo's terms if you use it commercially.
* App discovery uses `<queries>` intents, not `QUERY_ALL_PACKAGES`.

## Adding a language / city

* Language: copy `res/values/strings.xml` to `res/values-xx/strings.xml`, translate, then add
  `Lang("xx", "Name")` to `Languages.ALL` in `LocaleHelper.kt`.
* City: add a `City(name, lat, lon)` line to `Cities.ALL` in `Weather.kt`.

## Assumptions to verify on your device

Default favourites rely on these package names (check with `adb shell pm list packages`):
`com.google.android.youtube.tv`, `com.google.android.youtube.tvunplugged` (YouTube TV),
`com.netflix.ninja`, stock homes `com.google.android.apps.tv.launcherx` / `com.google.android.tvlauncher`.
Anything not installed is simply not shown; the user can add any app via Choose apps.

## Test checklist (not yet done)

1. Emulator "Android TV (1080p)" API 34 and an older one (API 21-28) - app starts, no crash.
2. D-pad: reach every tile, settings row and the bottom bar; no focus traps; BACK/HOME behave.
3. Launch YouTube/Netflix/Settings; return with HOME and BACK; focus returns to the same tile.
4. Text size x3, simple mode, language switch: layout not clipped at 960x540 dp.
5. Lock on/off; arrange mode cannot be left by D-pad until OK/BACK.
6. Install/uninstall an app while the launcher runs: lists update on next resume.
7. Memory: `adb shell dumpsys meminfo vn.simpletv.launcher` on the home screen
   (target: a few tens of MB; home screen only keeps a handful of icons).
8. Rotation / process restart: `adb shell am kill vn.simpletv.launcher`, press HOME.

## Known gaps

* The TV banner is a plain vector (house icon) without the app name text.
* No PIN lock; the lock switch only prevents accidental edits.
* Language list is a short fixed list (vi, en); other texts like city names are not translated.
