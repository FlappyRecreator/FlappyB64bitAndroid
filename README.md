# Flappy Bird 64-bit

An unofficial fan recreation of Flappy Bird, rebuilt from scratch for modern Android devices. It has an online leaderboard with verified scores, replays you can save as videos, and over a hundred optional settings.

<p align="center">
  <img src="https://github.com/user-attachments/assets/fd685be8-0166-4b06-a90b-afc2388b7258" alt="Flappy Bird 64-bit screenshot" width="295">
</p>

<p align="center">
  <b><a href="../../releases/latest">Download the latest version</a></b> · Free · No ads · Android 64-bit
</p>

<p align="center"><b>If you enjoy the project, please consider giving it a star!</b></p>

---

## Contents

- [About](#about)
- [What's new in v1.5.4](#whats-new-in-v154)
- [Features](#features)
- [Online leaderboard](#online-leaderboard)
- [Replays](#replays)
- [Installation](#installation)
- [Privacy](#privacy)
- [Reporting bugs](#reporting-bugs)
- [Contributing](#contributing)
- [Legal notice](#legal-notice)
- [Credits](#credits)

---

## About

Flappy Bird 64-bit is a free, non-commercial fan project that brings the 2013 mobile game Flappy Bird back to current Android phones and tablets.

The original game was removed from app stores in 2014. Its Android release was built for older 32-bit devices, and many modern phones can no longer install or run it. I started this project so the game could stay playable on today's hardware.

The game is written from scratch in Java and is modeled on version 1.3 of the original Android release. Out of the box it plays just like the original. If you want more, you can change nearly everything about how the game looks and feels, compete on a global leaderboard, and watch your best rounds again.

---

## Features

### Gameplay
- The classic tap-to-flap gameplay, faithful to the original by default
- An optional **Classic feel** preset that closely recreates the physics of the original 1.3 release, including gravity, flap strength, pipe layout, hitbox, scoring and bird tilt
- A **Classic** settings tab that shows every Classic feel setting in one place, with a counter for how many match
- Medals, high score, day and night backgrounds and three bird colors
- Original-style menu buttons, including the Rate button, the NEW label next to your best score and the leaderboard button

### Modern Android support
- Built for 64-bit devices and Android 17
- Works in portrait and landscape
- Supports phones, tablets, foldables, Chromebooks, split-screen and resizable windows
- Runs at your screen's full refresh rate, including 60, 90, 120, 144 and 240 Hz
- An optional frame rate limit of 30, 60, 90 or 120 fps to save battery, without changing game speed
- Adjusts for camera cutouts and system bars

### Keyboard, mouse and controllers
- Full support for USB and Bluetooth keyboards. Space flaps by default
- Every key can be changed, including flap, pause, play and settings
- Mouse support: click to flap and right-click to pause. The pointer hides automatically during a run
- The replay viewer works with keyboards, media keys and game controllers

### Customization
A full settings menu with a live preview that plays by itself and shows each change as you make it.

- **Bird:** color, tint, size, hitbox, wing speed, trail, rainbow and ghost effects, and starting position
- **World:** background, sky and ground speed, a day and night cycle, and rain, storm or snow
- **Pipes:** color, width, gap, spacing, height variation, moving pipes and golden pipes
- **Physics:** gravity, flap strength, game speed, a difficulty ramp, points per pipe and medal scores
- **Effects:** screen shake, motion blur, screen dimming and the crash flash
- **Modes:** God mode, extra lives, autopilot, jetpack, drunk mode, upside down and reverse pipes
- **System:** sound and weather volume, vibration, pause button, auto-pause, replays, FPS counter, hitbox outlines and keyboard controls
- **Advanced:** fixed-step physics at 30, 60 or 120 Hz with Smooth or Original timing, whole-pixel movement, fall speed limit, tilt behavior, ceiling and scoring point

### Settings tools
- An **All** tab that lists every setting on one page, with Reset to defaults at the top
- **Search** across every setting
- **Edit as text**, an options file showing every setting at once, with safe limits so mistakes can't break the game
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

---

## Online leaderboard

Choose a name and your best fair rounds are posted to a global leaderboard automatically.

- **All time** and **Weekly** boards, showing the top 10, 25, 50 or 100 players
- Your own place is always shown, even when you're outside the list
- Gold, silver and bronze medals for the top three, and a glowing gold name for first place
- Scores that went up since you last looked count up and slide to their new places, with light haptic ticks
- Scores played with Classic feel are labeled **Classic**
- Double-tap a row to see which version it was played on
- Opens instantly, even offline, using what you saw last time
- Names can be 1 to 16 characters, and rude names are refused

Leaderboard options are in **Settings > Leaderboard**.

### Verified scores and anti-cheat
Every posted score is checked by replaying the round exactly as it was played. Scores that pass get a **purple check mark**, and the leaderboard shows verified scores by default.

- Only fair rounds can be posted. Rounds played with God mode, autopilot, extra lives or other help stay off the leaderboard
- While you play, the game sends a small timing mark every few seconds, so a run must be played in real time. Slow motion is caught, even for a short stretch
- Each round's pipes come from a seed chosen at the start, and the checker makes sure the round started exactly like a fresh one
- Changed game files or modified physics are caught because the replay won't match the score
- Rejected scores are taken down, and players with 3 rejected runs in a week are banned automatically for 14 days
- Scores below 5 aren't posted

Turn on **Show unverified** on the leaderboard to also see scores that are still waiting to be checked.

---

## Replays

Every round is recorded in the background, using almost no battery or storage. Open them from the **Watch replays** card at the top of Settings.

- Keep your latest 1, 3, 5 or 10 rounds, or choose **Best only** to keep just your top round until you beat it
- Each replay shows a snapshot of the round and how long it lasted
- Play and pause, drag through the round, step frame by frame, and watch at 0.25x, 0.5x, 1x or 2x speed
- Double-tap the left or right side to skip 5 seconds, or the middle to play or pause
- Vibration plays just like the real round at normal speed
- Long-press a replay to delete it

### Save as video
- Save any replay as a video, with the game's sounds mixed in
- Video size: Full, 1080p or 720p
- Frame rate: 30 or 60 fps
- Quality: High, Very high or Best
- Fast hardware encoding, with a time-left estimate while it saves
- Share the video as soon as it's saved

### Replay keyboard shortcuts

| Key | Action |
|---|---|
| Space or K | Play or pause |
| Left / Right | Skip 5 seconds back or forward |
| J / L | Skip 10 seconds back or forward |
| , / . | Step one frame back or forward |
| Shift + Left / Right | Step one frame back or forward |
| [ / ] or - / = | Slower or faster |
| Home or 0 / End | Jump to the start or the end |
| M | Mute or unmute |
| S | Save as video |
| Esc | Close the replay |

Media keys work too, and on a controller, A plays or pauses and B closes.

---

## Installation

1. Download the latest APK from the [Releases page](../../releases/latest).
2. Open the downloaded file.
3. If Android asks, allow your browser or file manager to install apps from unknown sources.
4. Follow the on-screen prompts to finish installing.

> **About the Mediafire folder:** Much older versions are still available in the [Mediafire folder](https://www.mediafire.com/folder/at8q4prpl59m6/Flappy+Bird+64-bit+Recreation). Every release there is outdated, including the folder named "LATEST VERSION". I no longer have access to that account and cannot update it. For the current version, always use the **Releases** page.

### Updating
Install the new APK over the existing one. Your settings, high score, leaderboard name and replays are kept.

If Android shows "App not installed," uninstall the previous version first and then install the new one. Uninstalling deletes your settings, high score and replays. To keep your settings, use **Settings > Presets > Export to file** before uninstalling, then import the file afterward.

Please keep the game up to date. New versions often improve anti-cheat, and runs from much older versions may not be verified.

### Safety
The full source code is in this repository and can be reviewed by anyone.

Please download the app only from the **Releases** page of this repository. I am not responsible for copies distributed anywhere else. Each release lists the APK's SHA-256 checksum so you can confirm your copy is genuine.

Some antivirus apps flag apps installed from outside the Play Store. If you have any doubts, you can scan the APK with a service such as VirusTotal, or build the app yourself from the source code.

---

## Privacy

- No ads, in-app purchases, analytics or tracking
- No sign-up, email or password
- Your settings, high score, activity log, replays and saved videos are stored only on your device. The log never leaves your device unless you choose to export or share it

### What the leaderboard sends
The leaderboard is the only part of the game that goes online. Nothing is sent until you choose a leaderboard name.

When you choose a name, the game creates an anonymous ID with Google Firebase. It isn't linked to your Google account, email or phone number. While posting is on, the game sends:

- Your leaderboard name
- Your best all-time and weekly scores
- Basic details about those rounds: how long they lasted, how many times you flapped, time spent paused, whether Classic feel was on, and the app version
- A recording of the taps in your round, so it can be checked. Recordings are deleted once they've been checked
- Small timing marks while you play, which prove the round was played in real time

Entries are removed after 14 days without playing, unless they're in the top 100.

- **Stop posting:** turn off **Settings > Leaderboard > Post scores automatically**
- **Remove your entry:** use **Settings > About > Remove my leaderboard entry**. Your local high score is kept

---

## Reporting bugs

Please [open an issue](../../issues) and include the following:

- **Your activity log.** Export it from **Settings > Presets > Activity log** and attach the file. It already includes your device model, Android version and app version, and it's the most helpful thing you can send.
- **The steps that cause the problem.**
- **A screenshot or screen recording,** if possible. For gameplay bugs, a saved replay video is perfect.

If you'd rather not share the log, open **Settings > About > Copy app info** and paste the result into your issue instead.

---

## Contributing

Contributions are normally not accepted but feel free to fork or play around.
Please do not submit code, artwork or audio that you don't have the right to share.

---

## Legal notice

This is an unofficial, non-commercial fan project. It is not affiliated with, endorsed by or sponsored by Dong Nguyen, .GEARS or any current owner of the Flappy Bird name.

"Flappy Bird" and all related names, characters, artwork and sound effects belong to their respective owners. They are used only to identify the game this project recreates, and I claim no ownership of them.

This project is free and does not generate any revenue.

If you hold rights to any material in this project and would like it changed or removed, please contact me through an issue on this repository or through my GitHub profile. I will review every request and respond promptly.

The source code I wrote is shared under the terms of the LICENSE file in this repository. That license does not cover the Flappy Bird name or the original game's artwork and sounds.

---

## Credits

- **Breathe**: developer of Flappy Bird 64-bit.
- **Dong Nguyen (.GEARS)**: creator of the original Flappy Bird (2013), which inspired this project. All credit for the original game, its design and its artwork belongs to him.
- **Google**: for Android, Android Studio, Firebase and the Material Components library.
- **Everyone who tested, reported bugs and suggested features**: thank you for helping make this project better.
