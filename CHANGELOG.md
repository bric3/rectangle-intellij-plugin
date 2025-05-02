<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Rectangle Actions Plugin Changelog

## [Next]

### Fixed

- Properly hide inline actions without icons on hover.

## [0.0.5] - 2025-04-11

### Fixed

- Missing action key for smaller width and larger width actions.

## [0.0.4] - 2025-04-10
  
### Added

- Auto-add the Rectangle menu to the main menu bar.
- Support `smaller-width` and `larger-width` actions add in Rectangle 0.85 ([rxhanson/Rectangle#1504](https://github.com/rxhanson/Rectangle/pull/1504)), [#11](https://github.com/bric3/rectangle-intellij-plugin/issue/11).
- Suggest ignoring the IDE in Rectangle to avoid shortcut conflict if Rectangle is higher than 0.85, [#10](https://github.com/bric3/rectangle-intellij-plugin/issue/10). 

### Changed

- Bumped min IntelliJ platform to 2024.3.
- Align icon colors to IntelliJ's New UI themes.
- Do not show sixths in the main menu as they don't have icons.

### Fixed

- Action icons are now always shown in the main Rectangle menu.
- Rectangle detection is now more robust [#20](https://github.com/bric3/rectangle-intellij-plugin/issue/20).

## [0.0.3] - 2021-11-11

### Fixed

- 0.0.2 released failed due to API issues.
- Workaround for `actionPerformed` being override-only in 2024.3.
- Bump the minimum platform to 2024.2 due to `coroutineToIndicator` being internal before.

## [0.0.2] - 2021-11-10

### Added

- Suggest installing Rectangle via brew if not installed.
- Rotate some icons in portrait mode.

### Changed

- Better Rectangle detection.
- in 2024.3 always show icons in the menu.

## [0.0.1] - 2021-10-27 Initial version

### Added

- Rectangle detection
- Rectangle Actions for [Rectangle URL actions](https://github.com/rxhanson/Rectangle?tab=readme-ov-file#execute-an-action-by-url)
- Detects when ignored apps also ignore Drag and Snap. Suggest tweaking the default settings.
- Menu with all actions

[Next]: https://github.com/bric3/rectangle-intellij-plugin/compare/v0.0.4...HEAD
[0.0.4]: https://github.com/bric3/rectangle-intellij-plugin/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/bric3/rectangle-intellij-plugin/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/bric3/rectangle-intellij-plugin/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/bric3/rectangle-intellij-plugin/commits/v0.0.1