## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Standardizing Dropdowns
**Learning:** Custom `Row` + `clickable` implementations for dropdowns lack visual affordance (trailing arrow) and proper accessibility semantics. Material 3 `ExposedDropdownMenuBox` with `MenuAnchorType.PrimaryNotEditable` provides a standardized, accessible pattern out of the box.
**Action:** Replace custom text-based selectors with `ExposedDropdownMenuBox` (read-only mode) to improve discoverability and screen reader support.
