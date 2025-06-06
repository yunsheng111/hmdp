# Frontend User Info Page Enhancements - Architectural Notes

**Created:** [2024-07-30 10:22:00 +08:00]
**Author:** AR (Qitian Dasheng)

## Feature: Blogger Filter in Followed Notes

*   **Affected Components:**
    *   `user-info.html` (Vue.js instance, HTML structure)
    *   `user-info.css` (Styling for new filter elements)
*   **Data Dependencies:**
    *   **Critical:** Note objects fetched from `/blog/of/follow` API **must** include a reliable `userId` (or equivalent unique blogger identifier) for each note. This is essential for accurate client-side filtering.
    *   The existing `followUsers` array (containing `id`, `icon`, `nickName` for followed users) will be used to populate the blogger selection dropdown.
*   **Key Logic Changes (Vue.js):**
    *   Introduction of `selectedFollowedBloggerId` state.
    *   Modification of `updateBlogDisplay` to filter notes based on `selectedFollowedBloggerId` and `showUnreadOnly`.
    *   Handling of `el-select` change events.
*   **API Impact:**
    *   No direct changes to API *endpoints* are proposed from the frontend perspective *if* the `userId` is already present in the `/blog/of/follow` response.
    *   If `userId` is not present, an API modification to include it would be necessary. This would constitute a change to the API contract for `/blog/of/follow`.
*   **Styling Considerations:**
    *   The new filter UI elements must be integrated cleanly within the existing `.blog-tabs` structure.
    *   Custom styling for `el-select` options to display blogger avatar and name.

## Update Log

*   **[2024-07-30 10:22:00 +08:00]** - Initial document creation. Added note regarding critical `userId` dependency for blogger filter feature. (Reason: Initial analysis for user request)