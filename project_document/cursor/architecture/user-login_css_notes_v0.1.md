# Architecture Notes: user-login.html CSS Adjustments

## Version 0.1
*   **Created At:** [2024-07-27 10:05:00 +08:00]
*   **Author:** AR
*   **Reason for Creation:** Initial analysis for UI adjustment task HMDP-LoginUI-Adjust-001.

## Analysis Summary:

1.  **Home Icon Positioning (`.right-actions` & `.home-btn`):
    *   Relevant CSS (approx. line 110 in `user-login.html`):
        ```css
        .right-actions {
            display: flex;
            justify-content: flex-end;
            align-items: center; /* Ensure vertical alignment */
            padding-right: 15px; /* Move home icon left from the very edge */
        }
        ```
    *   **Observation:** The `padding-right: 15px;` on the container `.right-actions` controls the spacing of the home icon from the right edge of the header. Increasing this value will move the icon further left.

2.  **User Agreement Visibility & Page Length (`.agreement-container` & `.login-container`):
    *   Relevant CSS for `.login-container` (approx. line 40 in `user-login.html`):
        ```css
        .login-container {
            max-width: 450px;
            margin: 40px auto;
            background: white;
            border-radius: 8px;
            box-shadow: var(--box-shadow);
            overflow: hidden; /* Default to hidden for visual effects like border-radius with banners */
            position: relative;
        }
        ```
    *   Relevant CSS for `.agreement-container` (approx. line 302):
        ```css
        .agreement-container {
            margin-top: 20px;
            display: flex;
            align-items: flex-start;
            font-size: 13px;
            padding-bottom: 20px;
            visibility: visible;
            opacity: 1;
        }
        ```
    *   **Observation:** The `.agreement-container` is styled to be visible. The `overflow: hidden;` on `.login-container` is the likely cause of it being hidden if the content height exceeds the implicit height of the container.
    *   The media query for mobile (`@media (max-width: 480px)`, approx. line 325) already sets `overflow-y: auto;` and `height: 100vh;` for `.login-container`. This behavior (internal scrolling) is desired for desktop as well, to prevent page lengthening.

## Proposed Architectural Approach (for INNOVATE phase):

*   Modify `padding-right` for `.right-actions`.
*   Modify `overflow: hidden;` to `overflow-y: auto;` for `.login-container` in the default (desktop) styles. Consider if `overflow-x: hidden;` is also needed to prevent horizontal scrollbars if any unexpected wide content appears.
*   Verify impact on `border-radius` and `box-shadow` of `.login-container`.

## Update Log:
*   [2024-07-27 10:05:00 +08:00] - v0.1: Initial document creation and analysis. (AR) 