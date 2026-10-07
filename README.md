# Key Contacts

Android 4.4.4 / API 19 physical-key contacts and dialer.

The app is built without third-party runtime libraries and uses the Android framework only. Target SDK is 19 so runtime permissions are not required on the target device.

Main capabilities include contacts, T9 search, recent calls, favorites, dialer/redial, contact edit/add/delete, settings, physical-key navigation, and an API-19 compatible in-call overlay/activity path.


## Design system

All visuals come from one small design layer, so a change in one place updates every screen:

- `res/values/colors.xml`, `dimens.xml`, `styles.xml` – base tokens, spacing and the Holo-based theme.
- `util/Palette` – runtime colors for the three themes (dark / light / high contrast) and font scale.
- `util/Ui` + `util/ColorUtil` – drawables, focus states, dialogs, empty states and helpers.
- `widget/` – `FocusableRow` (list card), `SoftKeyBar` (3 soft keys), `ScreenHeader`, `AvatarView` (photo or initial), `IconView` (line icons).

Focus is always shown the same way: accent tint, accent outline and a marker on the start edge (rows), or a solid accent pill (soft keys).