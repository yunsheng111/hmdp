# Initial Architecture Notes for Admin Login

*   **Timestamp:** [2024-05-29 10:05:00 +08:00]
*   **Author:** AR
*   **Version:** v0.1

## Overview
The admin login functionality will leverage the existing database schema for `tb_admin_user`, `tb_admin_role`, and `tb_admin_user_role`.
Authentication will be handled by Sa-Token, as specified in the API design document.

## Key Components
*   **Controller:** `AdminController` (new or existing, to be determined)
*   **Service:** `AdminService` / `IAdminService` (new or existing)
*   **Mapper:** `AdminMapper` (likely existing, based on `Admin.java` entity)
*   **Entity:** `com.hmdp.entity.Admin` (provided)

## Database Considerations
*   Password in `tb_admin_user` is stored hashed. **Assumption: BCrypt will be used for hashing and verification.**
*   `status` field in `tb_admin_user` needs to be checked (0-normal, 1-disabled).
*   `deleted` field in `tb_admin_user` needs to be checked (0-not deleted, 1-deleted).
*   Roles will be fetched via `tb_admin_user_role` and `tb_admin_role` to populate `Admin.adminInfo.roles` (as a list of role codes).
*   **Note on `nickname`:** The `Admin.java` entity contains a `nickname` field not present in the `tb_admin_user` table schema. As the API response for login does not require `nickname`, this discrepancy can be addressed later if `nickname` becomes relevant for other admin functionalities. For login, it will be ignored.

## Update Log
*   [2024-05-29 10:15:00 +08:00] - v0.1.1: Updated by AR during RESEARCH phase.
    *   Reason: Added assumption for BCrypt and note on `nickname` field discrepancy based on team discussion.
*   [2024-05-29 10:05:00 +08:00] - v0.1: Initial draft created by AR during RESEARCH phase.
    *   Reason: Initial assessment based on provided `hmdp.sql` and `Admin.java`. 