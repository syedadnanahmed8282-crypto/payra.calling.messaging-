# Payra

Messaging + calling app (Kotlin, Jetpack Compose).

## How code gets in
Claude sends a ZIP. Upload the ZIP to the ROOT of this repo (Add file -> Upload files -> Commit to main).
The "Payra CI" workflow unpacks it, commits the files and builds a debug APK
(Actions -> latest run -> Artifacts -> `payra-debug-apk`).
If the build fails, download the `payra-build-log` artifact and send it to Claude.

To delete files, the ZIP can contain a `_delete.txt` with one path per line.
Workflow files (`.github/workflows/`) must be edited by hand on GitHub.
