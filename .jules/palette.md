## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Standardized Dropdowns
**Learning:** Custom text-based dropdowns reduce affordance. The `ExposedDropdownMenuBox` with `readOnly` text field and `MenuAnchorType.PrimaryNotEditable` provides better accessibility and visual consistency for selection inputs.
**Action:** Replace custom `Row` + `clickable` text selectors with `ExposedDropdownMenuBox`.
