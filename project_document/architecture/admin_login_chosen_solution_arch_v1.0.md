# Admin Login - Chosen Solution Architecture

*   **Timestamp:** [2024-05-29 10:30:00 +08:00]
*   **Author:** AR
*   **Version:** v1.0
*   **Based on:** Combination of Innovate Mode's Solution A (Standard Secure Implementation) and Solution C (Code Structuring for Extensibility).

## 1. Overview
This document outlines the chosen architectural approach for the HMDP Admin Login functionality. The primary goal is to deliver a secure, reliable, and maintainable login mechanism, adhering to industry best practices while ensuring clarity in code structure.

## 2. Core Principles Addressed
*   **Security:** Prioritized through BCrypt for password hashing, Sa-Token for JWT-based authentication, and secure handling of credentials.
*   **Maintainability & Extensibility (from Solution C):** Emphasis on clear separation of concerns in service layers (user retrieval, password validation, role fetching, token generation).
*   **Adherence to API Specification:** The implementation will strictly follow `管理员功能模块接口设计.md`.
*   **KISS & DRY:** Strive for simple, clear logic and avoid repetition.

## 3. Key Components & Flow

```mermaid
sequenceDiagram
    participant AdminClient as Admin Client
    participant AdminController as AdminController
    participant AdminService as IAdminService
    participant SaTokenUtil as Sa-Token Utilities
    participant AdminMapper as AdminMapper (MyBatisPlus)
    participant Database as Database (MySQL)

    AdminClient->>+AdminController: POST /api/admin/login (username, password)
    AdminController->>+AdminService: login(loginRequestDto)
    AdminService->>+AdminMapper: selectByUsername(username)
    AdminMapper->>+Database: Query tb_admin_user
    Database-->>-AdminMapper: AdminUser entity (or null)
    AdminMapper-->>-AdminService: AdminUser entity (or null)

    alt User Not Found or Invalid State
        AdminService-->>-AdminController: Throw AuthenticationException (e.g., "Invalid credentials" or "Account disabled/deleted")
        AdminController-->>-AdminClient: Error Response (e.g., 401/403, specific msg)
    else User Found & Active
        AdminService->>AdminService: verifyPassword(plainPassword, hashedPassword) using BCrypt
        alt Password Incorrect
            AdminService-->>-AdminController: Throw AuthenticationException ("Invalid credentials")
            AdminController-->>-AdminClient: Error Response (e.g., 401, "Invalid credentials")
        else Password Correct
            AdminService->>+AdminMapper: selectRolesByUserId(userId)
            AdminMapper->>+Database: Query tb_admin_user_role, tb_admin_role
            Database-->>-AdminMapper: List<RoleCode>
            AdminMapper-->>-AdminService: List<RoleCode>
            AdminService->>+SaTokenUtil: generateToken(userId, username, roles)
            SaTokenUtil-->>-AdminService: JWT Token
            AdminService->>AdminService: Log successful login (to tb_admin_audit_log - async if possible)
            AdminService-->>-AdminController: LoginResponseDto (token, adminInfo)
            AdminController-->>-AdminClient: Success Response (token, adminInfo)
        end
    end
```

## 4. Technology Stack & Key Choices
*   **Backend Framework:** Spring Boot
*   **Authentication/Authorization:** Sa-Token (for JWT generation, validation, and RBAC integration)
*   **Password Hashing:** BCrypt (e.g., using `SaSecureUtil.bcryptCheck` or a dedicated BCrypt library if Sa-Token's utility is not preferred/available for direct use in this manner. Spring Security's `BCryptPasswordEncoder` is also a common choice).
*   **ORM:** MyBatisPlus
*   **Database:** MySQL (as per `hmdp.sql`)
*   **Logging:** SLF4J + Logback (standard for Spring Boot). Audit logging to `tb_admin_audit_log` table.

## 5. Service Layer Design (Illustrative - from Solution C)
*   `IAdminService` will have methods like:
    *   `AdminLoginResponseDto login(AdminLoginRequestDto requestDto)`: Main login orchestrator.
    *   `Admin findValidAdminByUsername(String username)`: Retrieves user, checks status/deleted flags.
    *   `void verifyAdminPassword(String rawPassword, String encodedPassword)`: Password verification.
    *   `List<String> getAdminRoleCodes(Long adminId)`: Fetches role codes.
    *   `String generateAdminToken(Admin admin, List<String> roleCodes)`: Token generation via Sa-Token.
    *   `void recordLoginAudit(String username, boolean success, String ipAddress)`: Audit logging.

## 6. Error Handling
*   Use custom exceptions (e.g., `AdminAuthException`) extending Spring's `AuthenticationException` or similar for clear error propagation.
*   Global exception handler (`@ControllerAdvice`) to format error responses consistently as per API design.

## 7. Update Log
*   [2024-05-29 10:30:00 +08:00] - v1.0: Initial version created by AR, combining best of Solution A & C. Reason: Formalize chosen approach from INNOVATE phase. 