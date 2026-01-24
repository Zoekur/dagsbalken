## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-13 - Standardizing Dropdown Interaction
**Learning:** Custom text-based dropdown triggers lack affordance and accessibility traits (like screen reader announcements for expanded state).
**Action:** Use `ExposedDropdownMenuBox` with a read-only `OutlinedTextField` and `TrailingIcon` for all selection inputs to ensure consistent visual cues and accessibility behavior.
