# Context

Project_Name/ID: HMDP_UserInfo_Enhancement_001
Task_Filename: user_info_page_enhancement.md
Created_At: [2024-07-30 10:00:00 +08:00]
Creator: AI (Qitian Dasheng - PM drafted, DW organized)
Associated_Protocol: RIPER-5 + Multi-Dimensional Thinking + Agent Execution Protocol (Refined v3.8)
Project_Workspace_Path: `D:\workspace\hmdp\project_document\`

# 0. Team Collaboration Log & Key Decision Points
---
**Meeting Record**
* **Date & Time:** [2024-07-30 10:05:00 +08:00]
* **Meeting Type:** Task Kickoff/Requirements Clarification (Simulated)
* **Chair:** PM
* **Recorder:** DW
* **Attendees:** PM, PDM, AR, LD, UI/UX, TE, SE
* **Agenda Overview:**
    1. Review user request: Add blogger filter to "Followed" notes in user-info page.
    2. Analyze impact on existing HTML, CSS, and JS.
    3. Identify data dependencies (especially blogger ID in note objects).
    4. Discuss potential risks and mitigation.
    5. Outline next steps.
* **Discussion Points:**
    * PM: "New request is to allow filtering notes by specific followed bloggers on the user-info page."
    * PDM: "This enhances user experience by providing more focused content consumption from followed users."
    * AR: "Key dependency: each note from a followed blogger must contain a unique `userId` for accurate filtering. Current data seems to use `name` and `icon` but not `userId` explicitly in the `blogs2` items. This needs verification or API update."
    * LD: "Frontend will need a new state variable for the selected blogger, likely `selectedFollowedBloggerId`. The `el-select` component from Element UI should work well for the dropdown. `updateBlogDisplay` logic will need significant changes. CSS for the new filter bar needs careful handling within `.blog-tabs`."
    * UI/UX: "Dropdown options should show blogger's avatar and nickname. The filter bar must be visually balanced. Clear empty-state messages are crucial (e.g., 'Please select a blogger to see their notes')."
    * TE: "Test cases: no blogger selected; blogger selected with notes (all/unread); blogger selected with no notes; interaction with 'Show unread only' filter; dropdown updates correctly if user follows/unfollows someone."
    * SE: "No immediate security concerns identified, but ensure any new data fetched (like blogger list for dropdown) is handled securely."
* **Action Items/Decisions:**
    1.  (AR/LD) Verify if `userId` is present in note objects returned by `/blog/of/follow`. If not, flag as critical dependency requiring backend change or alternative (less reliable) frontend matching. **Decision for now: Proceed with assumption `userId` will be available on note objects as `blog.userId`.**
    2.  (DW) Document all findings and decisions.
* **DW Confirmation:** [Minutes complete and compliant with standards. [2024-07-30 10:15:00 +08:00]]
---

# Task Description
The user wants to add a filter to the "关注" (Followed) tab within the "笔记" (Notes) section of the `user-info.html` page. This filter will allow users to select a specific blogger they follow from a dropdown list. The dropdown should display the blogger's avatar and name. Once a blogger is selected, the note list below should display only the unread notes from that selected blogger. If no blogger is selected, a message like "你还未选择博主" (You haven't selected a blogger yet) should be displayed in the notes area. This new filter should be placed between the "我的 | 关注" sub-tabs and the "查看全部/只看未读" filter.

# Project Overview (Populated in RESEARCH or PLAN phase)
This task aims to enhance the user information page by providing a more granular filtering capability for notes from followed bloggers, improving content discovery and user experience.

---
*The following sections are maintained by AI during protocol execution. DW is responsible for overall document quality. All referenced paths are relative to `D:\workspace\hmdp\project_document\` unless specified. All documents should include an update log section where applicable.*
---

# 1. Analysis (RESEARCH Mode Population)
* **Requirements Clarification/Deep Dive:**
    * Add a dropdown filter for "关注的博主" (Followed Bloggers) in the "笔记" (Notes) -> "关注" (Followed) tab.
    * Location: Between the "我的 | 关注" sub-tabs (`.blog-tab-group`) and the "查看全部/只看未读" filter (`.read-filter`).
    * Dropdown Content: Each option should show the followed blogger's avatar and nickname.
    * Filtering Logic:
        * Default: If no blogger is selected from the dropdown, the notes list area should display a message: "你还未选择博主".
        * On Selection: Display notes from the selected blogger. Initially, show only *unread* notes from that blogger.
        * Interaction with existing "查看全部/只看未读" (`.read-filter`): This filter should now apply to the *selected blogger's* notes. If "查看全部" is active, show all notes from the selected blogger. If "只看未读" is active, show only unread notes from the selected blogger.
    * Data for Dropdown: Needs a list of followed bloggers, including their ID, avatar, and nickname. This is likely available via `this.followUsers`.
    * Data for Filtering Notes: Each note object in `this.blogs2` (or `this.allBlogs2`) **must contain a `userId` field (or similar identifier for the blogger)** to allow filtering by the selected blogger. Currently, `blogs2` items have `b.name` and `b.icon`, but a reliable ID is crucial.
* **Code/System Investigation (`user-info.html`, `user-info.css`):**
    * **HTML (`user-info.html`):**
        * The target insertion point is within the `.blog-tabs` div, after `.blog-tab-group` and before `.read-filter`.
        * An Element UI `el-select` component will be suitable for the dropdown.
        * A new conditional display area for the "你还未选择博主" message will be needed.
    * **CSS (`user-info.css`):**
        * Styles for the new "关注的博主：" label and the `el-select` dropdown will be needed.
        * The layout of `.blog-tabs` will need adjustment to accommodate the new filter.
        * Styles for the dropdown options (avatar + name).
    * **JavaScript (Vue instance in `user-info.html`):**
        * **Data:**
            * Add `selectedFollowedBloggerId: null` (or similar) to store the ID of the selected blogger.
            * `followUsers` (already exists, fetched in `queryFollowUsers`) seems to be the source for dropdown options. Each item should have `id`, `icon`, `nickName`.
        * **Computed Properties (or Methods):**
            * A computed property might be needed to format `followUsers` for the `el-select` options if custom rendering is used for avatar + name.
            * The logic for `blogs2` (displayed notes) will need to be heavily modified based on `selectedFollowedBloggerId` and `showUnreadOnly`. The `updateBlogDisplay` method is the primary candidate for this logic.
        * **Methods:**
            * `queryBlogsOfFollow()`: This method fetches notes of followed users. It's critical that the notes returned by the API `/blog/of/follow` include the `userId` of the blogger who posted the note.
            * `updateBlogDisplay()`: This method will now need to:
                1. Check if `selectedFollowedBloggerId` is set.
                2. If not, set `blogs2` to an empty array and show the "未选择博主" message.
                3. If set, filter `allBlogs2` for notes where `blog.userId === selectedFollowedBloggerId`.
                4. Then, further filter these notes based on `showUnreadOnly` (if `!blog.isRead`).
            * A new method to handle the change event of the `el-select` for bloggers.
            * When `activeSubTab` changes to 'follow', or `followUsers` data updates, the blogger dropdown should be populated/updated.
* **Technical Constraints & Challenges:**
    * **Critical Dependency:** Availability of `userId` (or a unique blogger identifier) on each note object in `blogs2` / `allBlogs2`. If this is not present, filtering will be unreliable or impossible. **Assumption:** `blog.userId` will be available.
    * CSS complexity for responsive and clean layout of the new filter within `.blog-tabs`.
    * Ensuring the `el-select` dropdown displays avatars correctly within its options.
* **Implicit Assumptions:**
    * The `followUsers` array contains all necessary information (ID, avatar, nickname) for the dropdown.
    * The API `/blog/of/follow` returns notes with a clear `userId` for each note's author.
* **Early Edge Case Considerations (TE):**
    * User has no followed bloggers: Dropdown should be empty or disabled, with an appropriate message. (Current `followUsers.length === 0` handles the main list, this should extend to the dropdown).
    * Selected blogger has no notes / no unread notes: The notes list should show an appropriate empty state message (potentially reusing/adapting `.empty-blog-tip`).
    * User follows/unfollows someone: The blogger dropdown list should update.
* **Preliminary Risk Assessment:**
    * High Risk: Missing `userId` in note objects from `/blog/of/follow` API. This would require backend changes.
    * Medium Risk: CSS layout issues, especially on different screen sizes.
    * Low Risk: Complexity in Vue.js logic for filtering, manageable with careful implementation.
* **Knowledge Gaps:**
    * Confirmation of `userId` field in note objects from `/blog/of/follow` API.
* **Architectural Documents (AR):**
    * No specific architectural documents seem to be directly impacted for this frontend-focused change, assuming the API contract for `/blog/of/follow` can support the `userId` requirement. If API changes are needed, then API documentation would need updating. An initial note will be added to `/project_document/architecture/frontend_user_info_enhancements.md`.
* **DW Confirmation:** This section is complete, clear, synced, and meets documentation standards. [2024-07-30 10:20:00 +08:00]

# 2. Proposed Solutions (INNOVATE Mode Population)
* (To be populated)

# 3. Implementation Plan (PLAN Mode Generation - Checklist Format)
* (To be populated)

# 4. Current Execution Step (EXECUTE Mode - Updated when starting a step)
* (To be populated)

# 5. Task Progress (EXECUTE Mode - Appended after each step/node)
* (To be populated)

# 6. Final Review (REVIEW Mode Population)
* (To be populated)

