# Changelog

## [1.2.0] - 2025-12-01

### Added
- **Gem Compacting Upgrade**: Automatically compacts three lower-tier Mine and Slash gems into one higher-tier gem
- Full event and tick handling support (IInsertResponseUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade)
- Japanese and English language support
- Updated documentation and mod description

### Technical Details
- Implemented GemCompactingUpgradeWrapper following CompactingUpgrade structure
- Uses slotsToCompact mechanism for GUI and tick-based processing
- Integrates with MAS GemItem, GemRank, and GemType systems