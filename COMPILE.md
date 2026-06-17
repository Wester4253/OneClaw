# Compile OneClaw

## Debug build

1. Edit [app/build.gradle.kts](app/build.gradle.kts).
2. Change `versionCode` and `versionName` in `defaultConfig`.
3. Run:

```bash
ANDROID_HOME=$HOME/Android/Sdk ./gradlew :app:assembleDebug
```

4. Find the APK in:

```text
app/build/outputs/apk/debug/
```

## Release build

1. Edit [app/build.gradle.kts](app/build.gradle.kts).
2. Change `versionCode` and `versionName` in `defaultConfig`.
3. Make sure release signing is set up in `local.properties` or environment variables:
   - `KEYSTORE_FILE`
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
4. Run:

```bash
ANDROID_HOME=$HOME/Android/Sdk ./gradlew :app:assembleRelease
```

5. Find the signed APK in:

```text
app/build/outputs/apk/release/
```

## Notes

- `local.properties` is not committed.
- If Gradle cannot find the Android SDK, set `sdk.dir` in `local.properties` or use `ANDROID_HOME=$HOME/Android/Sdk`.
