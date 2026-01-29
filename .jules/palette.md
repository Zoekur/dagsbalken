## 2025-12-12 - Color Selection Accessibility
**Learning:** Using `Role.RadioButton` on custom color selectors is good, but color alone is insufficient for state indication. Adding a high-contrast visual marker (like a checkmark) ensures the selected state is visible to all users, not just those with perfect color vision.
**Action:** Always overlay a contrast-calculated icon or border modification on color selection controls to indicate the active state.

## 2025-12-12 - Input Constraint Feedback
**Learning:** Silently blocking input (e.g., max length) confuses users. They may think the app is frozen or the keyboard is broken.
**Action:** Always provide a visual indicator (like a character counter) when enforcing input limits so the constraint is transparent.
