## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Read-Only Dropdown Pattern
**Learning:** For read-only selection menus, `ExposedDropdownMenuBox` with an `OutlinedTextField` (readOnly = true) is the standard Material 3 pattern. It provides better accessibility (labeling, semantics) than custom clickable rows.
**Action:** Use `Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)` on the text field anchor to ensure proper behavior in newer Material 3 versions.
