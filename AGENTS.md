# Payra - working rules

- App: Payra (package `com.payra.calling`), native Kotlin + Jetpack Compose, minSdk 23.
- Backend: Supabase project "Payra" (ap-southeast-1), Cloudinary, Agora, Firebase Cloud Messaging.
- One change at a time. Verify against real code, logs and data before fixing; do not guess.
- Do not change the pinned build versions in `gradle/libs.versions.toml` or the CI workflow without a reason.
- Never put secrets (service keys, API secrets, Firebase service-account JSON) in this repo.
  Only the public Supabase URL and anon (publishable) key may appear in the app.
- Design source: Figma file "Chat App UI - Liquid Glass" (screens 1-68 + Style Guide).
- Chat media (photos / videos / voice) is auto-deleted after about 72 hours by a shared cleanup job.
  Profile, cover and wallpaper files live under protected folders (`profiles/payra/`, `covers/payra/`, `chat_wallpapers/payra/`).

## Glass and motion rules (the app's main attraction)
- All glass goes through `GlassSurface` (`designsystem/Glass.kt`). Bars that content scrolls under
  (headers, message bar, tab bar) go inside `GlassScaffold`, so the content shows through the glass
  (real blur on Android 12+, stronger frosted fill below).
- Every tappable element uses `bouncyClick` (press squish). Every screen animates in: `enterFromBottom(index)`
  for staggered entrances, `popIn` for bubbles / avatars, `shake` for errors, springs (not linear tweens).
- New or changed UI must include its motion in the same change: entrance, press feedback, state changes
  (AnimatedVisibility / AnimatedContent / animate*AsState), list changes (`animateItem()`), screen transitions.
- Motion helpers live in `designsystem/Motion.kt`; extend them instead of writing one-off animations.
