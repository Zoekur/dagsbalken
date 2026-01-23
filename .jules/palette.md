## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-13 - Input Character Limits and Feedback
**Learning:** Users need immediate feedback on input constraints. Combining a strict character limit with a visible counter in `supportingText` prevents error states before they happen.
**Action:** Always pair `supportingText` counters with `maxLength` logic in text fields.
