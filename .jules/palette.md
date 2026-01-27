## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-13 - Standardized Selection Controls
**Learning:** Replacing custom `clickable` text elements with `ExposedDropdownMenuBox` significantly improves discoverability and accessibility by providing standard visual affordances (outline, label, trailing icon) and semantic roles.
**Action:** Use `ExposedDropdownMenuBox` with a read-only `OutlinedTextField` for all single-select configurations instead of custom dropdown implementations.
