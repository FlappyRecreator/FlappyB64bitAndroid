# Flappy Bird 64-bit

An unofficial fan recreation of Flappy Bird, rebuilt for modern Android devices.

<p align="center">
  <img src="https://github.com/user-attachments/assets/fd685be8-0166-4b06-a90b-afc2388b7258" alt="Flappy Bird 64-bit screenshot" width="295">
</p>

<p align="center"><b>If you enjoy the project, please consider giving it a star!</b></p>

---

## About

Flappy Bird 64-bit is a free, non-commercial fan project that brings the 2013 mobile game Flappy Bird back to current Android phones and tablets.

The original game was removed from app stores in 2014. Its Android release was built for older 32-bit devices, and many modern phones can no longer install or run it. I started this project so the game could stay playable on today's hardware.

The game is written from scratch in Java and is modeled on version 1.3 of the original Android release. Out of the box it plays just like the original. For players who want more, a full set of optional features and settings lets you change nearly everything about how the game looks and feels.

---

## Features

### Gameplay
- The classic tap-to-flap gameplay, faithful to the original by default
- An optional **Classic feel** preset that closely recreates the physics of the original 1.3 release, including gravity, flap strength, pipe layout, hitbox, scoring and bird tilt
- Medals, high score, day and night backgrounds and three bird colors
- Original-style menu buttons, including the Rate button, the NEW label next to your best score and the leaderboard button

### Modern Android support
- Built for 64-bit devices and Android 17
- Works in portrait and landscape
- Supports phones, tablets, foldables, Chromebooks, split-screen and resizable windows
- Runs smoothly on 60, 90, 120 and 144 Hz displays
- Adjusts for camera cutouts and system bars

### Keyboard and mouse
- Full support for USB and Bluetooth keyboards. Space flaps by default
- Every key can be changed, including flap, pause, play and settings
- Mouse support: click to flap and right-click to pause. The pointer hides automatically during a run

### Customization
A full settings menu with a live preview that plays by itself and shows each change as you make it.

- **Bird:** color, tint, size, hitbox, wing speed, trail, rainbow and ghost effects, and starting position
- **World:** background, sky and ground speed, a day and night cycle, and rain, storm or snow
- **Pipes:** color, width, gap, spacing, height variation, moving pipes and golden pipes
- **Physics:** gravity, flap strength, game speed, a difficulty ramp, points per pipe and medal scores
- **Effects:** screen shake, motion blur, screen dimming and the crash flash
- **Modes:** God mode, extra lives, autopilot, jetpack, drunk mode, upside down and reverse pipes
- **System:** sound and weather volume, vibration, pause button, auto-pause, FPS counter, hitbox outlines and keyboard controls
- **Advanced:** fixed 60 Hz physics, whole-pixel movement, fall speed limit, tilt behavior, ceiling and scoring point

### Settings tools
- An **All** tab that lists every setting on one page, with Reset to defaults at the top
- **Search** across every setting
- **Edit as text**, an options file showing every setting at once, with safe limits so mistakes cannot break the game
- Three save slots for your favorite setups
- Export and import all settings as a `.json` file
- Share your setup as text, or paste one someone sent you
- Undo for randomize, reset, load, import and preset changes
- Reset your settings or your high score separately

### Fair high scores
- The game keeps a separate record of your best score **earned by playing**
- If a high score was changed by hand or reached with help, such as autopilot, God mode or extra lives, the settings screen says so
- God mode runs end when the bird stays off the screen too long, so their scores can still be saved

### Built-in bug reporting
- An **Activity log** records rounds, setting changes and crashes, along with useful details about your device and app version
- The log can be viewed, exported as a `.txt` file or shared from **Settings > Presets > Activity log**
- The **About** tab shows the exact app version and device details, and can copy them for a report
- The log can be turned off in **Settings > Advanced**

### Privacy
- No ads, in-app purchases or accounts
- No analytics or tracking
- Your settings, high score and activity log are stored only on your device. The log never leaves your device unless you choose to export or share it

---

## Installation

1. Download the latest APK from the **Releases** page of this repository.
2. Open the downloaded file.
3. If Android asks, allow your browser or file manager to install apps from unknown sources.
4. Follow the on-screen prompts to finish installing.

> **About the Mediafire folder:** Much older versions are still available in the [Mediafire folder](https://www.mediafire.com/folder/at8q4prpl59m6/Flappy+Bird+64-bit+Recreation). Every release there is outdated, including the folder named "LATEST VERSION". I no longer have access to that account and cannot update it. For the current version, always use the **Releases** page.

### Updating
Install the new APK over the existing one. If Android shows "App not installed," uninstall the previous version first and then install the new one.

Uninstalling deletes your settings and high score. To keep your settings, use **Settings > Presets > Export to file** before uninstalling, then import the file afterward.

---

## Safety

The full source code is in this repository and can be reviewed by anyone.

Please download the app only from the **Releases** page of this repository. I am not responsible for copies distributed anywhere else.

Some antivirus apps flag apps installed from outside the Play Store. If you have any doubts, you can scan the APK with a service such as VirusTotal, or build the app yourself from the source code.

---

## Reporting Bugs

Please open an issue and include the following:

- **Your activity log.** Export it from **Settings > Presets > Activity log** and attach the file. It already includes your device model, Android version and app version, and it is the most helpful thing you can send.
- **The steps that cause the problem.**
- **A screenshot or screen recording,** if possible.

If you would rather not share the log, open **Settings > About > Copy app info** and paste the result into your issue instead.

---

## Contributing

Contributions are welcome, including bug fixes, accuracy improvements, performance work and new options.

1. Fork the repository.
2. Create a new branch for your changes.
3. Commit your changes with clear, descriptive messages.
4. Push the branch to your fork.
5. Open a pull request that explains what you changed and why. For visual or gameplay changes, please include screenshots or a short video.

I am still learning GitHub, so if your pull request needs anything special to merge correctly, please include brief instructions.

Please do not submit code, artwork or audio that you do not have the right to share.

---

## Legal Notice

This is an unofficial, non-commercial fan project. It is not affiliated with, endorsed by or sponsored by Dong Nguyen, .GEARS or any current owner of the Flappy Bird name.

"Flappy Bird" and all related names, characters, artwork and sound effects belong to their respective owners. They are used only to identify the game this project recreates, and I claim no ownership of them.

This project is free and does not generate any revenue.

If you hold rights to any material in this project and would like it changed or removed, please contact me through an issue on this repository or through my GitHub profile. I will review every request and respond promptly.

The source code I wrote is shared under the terms of the LICENSE file in this repository. That license does not cover the Flappy Bird name or the original game's artwork and sounds.

---

## Credits

- **Breathe**: developer of Flappy Bird 64-bit.
- **Dong Nguyen (.GEARS)**: creator of the original Flappy Bird (2013), which inspired this project. All credit for the original game, its design and its artwork belongs to him.
- **Google**: for Android, Android Studio and the Material Components library.
- **Everyone who tested, reported bugs and suggested features**: thank you for helping make this project better.
