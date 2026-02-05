## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Standardizing Dropdowns
**Learning:** Custom dropdowns using `Row` + `Text` lack accessibility affordances (like expanded state announcements) and visual cues (arrows). `ExposedDropdownMenuBox` standardizes this behavior.
**Action:** Replace custom text-based selectors with `ExposedDropdownMenuBox` and `MenuAnchorType.PrimaryNotEditable` for read-only choices.
