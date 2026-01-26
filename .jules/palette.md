## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Exposed Dropdown Menus
**Learning:** Raw `DropdownMenu` triggered by clickable text lacks visual affordance and accessibility traits of a form field.
**Action:** Use `ExposedDropdownMenuBox` with a read-only `OutlinedTextField` anchor for selection inputs to ensure consistency and accessibility.
