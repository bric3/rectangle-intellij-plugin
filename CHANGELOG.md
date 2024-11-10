<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Rectangle Actions Plugin Changelog

## [Next]

### Fixed

- Workaround for actionPerformed being override-only in 2024.3.
- Bump minimum platform to 2024.2 due to `coroutineToIndicator` being internal before.

## 0.0.2 - 2021-11-10

### Added

- Suggest installing Rectangle via brew if not installed.
- Rotate some icons in portrait mode.

### Changed

- Better Rectangle detection.
- in 2024.3 always show icons in the menu.

## 0.0.1 - 2021-10-27 Initial version

### Added

- Rectangle detection
- Rectangle Actions for [Rectangle URL actions](https://github.com/rxhanson/Rectangle?tab=readme-ov-file#execute-an-action-by-url)
- Detects when ignored apps also ignore Drag and Snap. Suggest tweaking the default settings.
- Menu with all actions
