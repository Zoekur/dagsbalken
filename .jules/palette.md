## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Standard Dropdown Components
**Learning:** Custom implementations of dropdowns using clickable text rows often lack proper accessibility roles and visual affordances (like chevrons).
**Action:** Replace custom text-based selectors with Material 3 `ExposedDropdownMenuBox` to ensure standard keyboard navigation, screen reader announcements, and clear visual state.
