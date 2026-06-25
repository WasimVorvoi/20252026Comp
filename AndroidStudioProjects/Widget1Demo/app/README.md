# Widget1Demo (Switch, EditText, SeekBar)

This app demonstrates three basic Android widgets and their interactions:

- Switch (`switch1`): toggles a label between "ON" and "OFF".
- EditText (`editTextTextPassword`): echoes the typed text to a label in real time.
- SeekBar (`seekBar`): displays the current progress as a percentage.

## Files of interest

- `src/main/java/com/example/widget1demo/MainActivity.java` — Wires up listeners and initializes UI state.
- `src/main/res/layout/activity_main.xml` — Layout containing the Switch, EditText, SeekBar, and TextViews.
- `src/main/AndroidManifest.xml` — Declares `MainActivity` as the launcher activity.

## How to run

1. Open the project root (`Widget1Demo`) in Android Studio.
2. Ensure an emulator or physical device is set up.
3. From the run configurations, select `app` and click Run.

### Command line (optional)

If you prefer the command line:

```powershell
# From the project root
C:\Users\30002804\AndroidStudioProjects\Widget1Demo\gradlew.bat :app:assembleDebug
```

The APK will be generated under `app/build/outputs/apk/debug/`.

## Notes

- The labels are initialized on launch so they reflect the current widget states before any interaction.
- You can adjust the layout/strings to suit your exercise or localization needs.
