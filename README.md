# User Service - RentMate Platform

A comprehensive microservice for managing users, authentication, identity verification, ratings, and reporting in the RentMate rental platform.

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Event Publishing](#-event-publishing)
- [Security](#-security)
- [Scheduled Tasks](#-scheduled-tasks)
- [Configuration](#-configuration)

## 🎯 Overview

The User Service is a Spring Boot microservice that handles all user-related operations in the RentMate platform. It provides a secure, scalable solution for user management, authentication, identity verification, trust building through ratings, and dispute resolution through a comprehensive reporting system.

## ✨ Features

### 🔐 Authentication & Authorization

#### JWT-Based Authentication
- **Dual Token System**:
    - **Access Token**: Short-lived JWT (60 minutes default) for API authentication
    - **Refresh Token**: Long-lived token (30 days default) stored securely with SHA-256 hashing
- **Stateless Authentication**: No server-side session storage, scalable across instances
- **Automatic Token Refresh**: Seamless user experience with token renewal without re-login
- **Session Management**: Track and invalidate active sessions per device

#### Password Security
- **Bcrypt Hashing**: Industry-standard password hashing with automatic salt generation
- **Secure Password Reset**:
    - Time-limited tokens (60 minutes expiration)
    - One-time use enforcement
    - SHA-256 token hashing for secure storage
    - All active sessions terminated on successful reset
- **Email Delivery**: Integration with notification service for secure token delivery
- **Privacy Protection**: Always returns success response regardless of email existence (prevents user enumeration)

#### Role-Based Access Control (RBAC)
- **Four-Tier Role Hierarchy**:
    1. **USER**: Basic access to profile, ratings, and own reports
    2. **DELIVERY_GUY**: USER privileges + report submission capabilities
    3. **ADMIN**: DELIVERY_GUY privileges + verification and report management
    4. **MANAGER**: ADMIN privileges + user role management and account creation

- **Method-Level Security**: Spring Security's `@PreAuthorize` annotations for fine-grained access control
- **Role Immutability**: Email and role cannot be changed through standard profile updates
- **Hierarchical Permissions**: Higher roles inherit lower role permissions

### 👤 User Management

#### Profile Management
- **Dual Profile Views**:
    - **Private Profile**: Complete information including email, phone, account status (own profile only)
    - **Public Profile**: Limited information (username, verification status, ratings) visible to all authenticated users

- **Profile Operations**:
    - Update personal details (first name, last name, phone number)
    - View trust metrics (average rating, total ratings)
    - Track membership duration
    - Monitor account activity status

#### Account Status Control
- **Four Status Levels**:
    - **ACTIVE**: Normal operation, full platform access
    - **INACTIVE**: Self-deactivated, no platform access
    - **PENDING_REPORT_REVIEW**: Temporary suspension during investigation
    - **SUSPENDED_BY_ADMIN**: Administrative suspension with session termination

- **Soft Delete Implementation**:
    - Accounts marked as `is_disabled = true` instead of deletion
    - Preserves data integrity for historical transactions
    - Maintains audit trail for compliance
    - All active sessions automatically invalidated

#### Administrative Controls
- **Advanced User Search**:
    - Filter by role, verification status, account status
    - Keyword search across name, email, phone number (minimum 2 characters)
    - Pagination with configurable page size (max 100 items)
    - Sortable by creation date, update date, rating, name

- **Status Management**:
    - Update account status with mandatory reason documentation
    - Automatic session termination on suspension
    - Event publishing for cross-service notification

- **Role Management**:
    - Promote/demote users with hierarchy enforcement
    - Create staff accounts (Delivery Guy, Admin) directly
    - Prevent self-demotion and invalid role transitions

### 🆔 Identity Verification

#### Document Verification Workflow
- **Multi-Step Process**:
    1. **Pre-Upload Validation**: Check eligibility before document upload
    2. **Secure Upload**: Generate time-limited Cloudinary pre-signed URLs
    3. **Document Submission**: Submit verification request with image URLs and ID number
    4. **Admin Review**: Manual verification by administrators
    5. **Decision**: Approve or reject with detailed feedback

#### Upload Security
- **Cloudinary Integration**:
    - Pre-signed upload URLs with timestamp and signature
    - Separate URLs for front and back ID images
    - Time-limited upload capability (prevents replay attacks)
    - Organized folder structure (`verifications/`)
    - Image URL validation to ensure Cloudinary origin

#### Submission Controls
- **Eligibility Checks**:
    - Prevents duplicate submissions (no PENDING or APPROVED requests allowed)
    - Enforces 48-hour cooldown after rejection
    - Returns clear feedback with next submission date
    - Real-time validation before upload initiation

- **Data Validation**:
    - ID number must be exactly 14 digits
    - Both image URLs required
    - Image URL format validation
    - Cloudinary URL verification (configurable)

#### Admin Review Process
- **Verification Queue**:
    - Paginated list of pending requests
    - Filter by status (PENDING, APPROVED, REJECTED)
    - Sort by submission date or review date
    - Access to complete submission history

- **Review Actions**:
    - **Approval**:
        - Sets `users.is_identity_verified = TRUE`
        - Records reviewer and timestamp
        - Publishes approval event for notification
    - **Rejection**:
        - Requires detailed rejection reason
        - Sets 48-hour cooldown period
        - Prevents immediate resubmission
        - Publishes rejection event with reason

- **Audit Trail**:
    - Complete history of all verification attempts
    - Reviewer identification
    - Timestamps for all status changes
    - Rejection reasons stored permanently

### ⭐ Trust & Ratings System

#### Rating Submission
- **Post-Transaction Ratings**:
    - 1-5 star rating system
    - Optional text feedback (unlimited length)
    - Bidirectional ratings (both parties can rate)
    - Tied to specific rental transactions

- **Rating Constraints**:
    - One rating per rental per user (enforced by unique constraint)
    - Rental must be COMPLETED before rating
    - Cross-service validation with Rental Service
    - Rated user must exist in system

#### Rating Management
- **Update Capability**:
    - Original rater can modify rating and feedback
    - Ownership verification (only rater can update)
    - Automatic rating recalculation on update
    - Timestamp tracking for updates

- **Delete Functionality**:
    - Hard delete from database (permanent removal)
    - Ownership verification required
    - Automatic rating recalculation on deletion
    - Use cases: error correction, rating retraction

#### Trust Metrics
- **Denormalized Ratings**:
    - `average_rating`: Real-time calculated average (0.00-5.00)
    - `total_ratings`: Count of ratings received
    - Updated automatically via database trigger on insert/update/delete
    - Instant availability for public profiles

- **Rating Display**:
    - Paginated list of received ratings
    - Sorted by creation date (most recent first)
    - Includes rater information and feedback text
    - Visible to all authenticated users

- **Reputation Building**:
    - Visible on public profiles
    - Influences trust decisions
    - Historical feedback preservation
    - Transparent rating system

### 🚨 Reporting System

#### Report Types
1. **FRAUD**: Item delivered is different/broken from listing
2. **DAMAGE**: Item returned with damage (requires damage percentage)
3. **OVERDUE**: Renter cannot be contacted for item return
4. **FAKE_USER**: User didn't show up at delivery location
5. **THIEVING**: Auto-escalated from OVERDUE or manually created for severe cases

#### Report Submission
- **Access Control**:
    - DELIVERY_GUY role required for submission
    - Must be associated with delivery transaction
    - Cross-service validation with Rental and Delivery services

- **Required Information**:
    - Reporter and reported user IDs
    - Report type selection
    - Detailed description (minimum 20-60 characters depending on type)
    - Related rental and delivery IDs
    - Damage percentage (for DAMAGE type)

- **Business Rules**:
    - Reporter must be item owner for DAMAGE reports
    - Reported user must be the renter
    - Rental must be actually overdue for OVERDUE reports
    - Duplicate reports for same delivery prevented

#### Report Lifecycle Management

**Status Flow**:
```
PENDING → UNDER_REVIEW → RESOLVED/DISMISSED
```

**Admin Workflow**:

1. **Claim Report**:
    - Locks report for exclusive access
    - Changes status to UNDER_REVIEW
    - Sets 30-minute lock timer
    - Records claiming admin and timestamp
    - Prevents concurrent modifications

2. **Lock Management**:
    - **30-Minute Auto-Expiration**: Releases lock if admin becomes inactive
    - **Manual Release**: Admin can release lock before completion
    - **Lock Refresh**: Extend lock for complex investigations (recommended every 25 minutes)
    - **Lock Verification**: Only lock holder can modify report

3. **Resolution**:
    - **Resolve**: Mark as RESOLVED after investigation and action
        - Records resolution timestamp and admin
        - Releases lock automatically
        - Triggers deposit transfer for FRAUD/DAMAGE (via Payment Service)
        - Sends notifications to all parties

    - **Dismiss**: Mark as DISMISSED if unfounded
        - Requires detailed dismissal explanation
        - Restores reported user's account status to ACTIVE
        - Documents reasoning for audit
        - Releases lock automatically

#### Report Views
- **Submitted Reports**: View reports filed by current user
- **Received Reports**: View reports filed against current user
- **All Reports** (Admin): Complete list with advanced filtering
- **Available Reports** (Admin): Claimable reports (PENDING and unlocked)

#### Advanced Features

**Automatic Escalation**:
- **Trigger**: OVERDUE reports older than 72 hours
- **Action**:
    - Creates new THIEVING report with detailed escalation notes
    - Marks original report as RESOLVED
    - Links reports via `auto_escalated_from`
    - Suspends reported user's account (`SUSPENDED_BY_ADMIN`)
    - Disables reported user's access
- **Frequency**: Checked every 3 hours by scheduled job
- **Purpose**: Escalates serious situations to appropriate severity level

**Lock Cleanup**:
- **Automatic Release**: Expired locks released every 30 minutes
- **Status Reset**: Reports changed back to PENDING
- **Queue Return**: Reports become available for claiming
- **Prevents Deadlock**: Ensures abandoned reports are accessible

**Cross-Service Integration**:
- **Rental Service**: Validates rental exists and status
- **Delivery Service**: Validates delivery association (planned)
- **Payment Service**: Triggers deposit transfers on resolution (planned)

## 🛠 Technology Stack

- **Java 25**
- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **Flyway** for database migrations
- **RabbitMQ** for event-driven architecture
- **Cloudinary** for image storage
- **OpenFeign** for service-to-service communication
- **SpringDoc OpenAPI** for API documentation
- **Maven** for dependency management

## 🏗 Architecture

### Microservice Communication
```
User Service
    ├── REST API (Controller Layer)
    ├── Business Logic (Service Layer)
    ├── Data Access (Repository Layer)
    └── Configuration 
        ├── Security
        └── Cloudinary (Image Storage)
```

### Key Components

- **Controllers**: RESTful endpoints for authentication, users, verification, ratings, and reports
- **Services**: Business logic implementation with transaction management
- **Repositories**: JPA repositories with custom queries and specifications
- **Security**: JWT filter, authentication provider, and access control
- **Schedulers**: Automated tasks for report escalation and lock cleanup
- **Event Publishers**: RabbitMQ integration for cross-service communication
- **Mappers**: DTO/Entity mapping with clean separation of concerns
- **Specifications**: Dynamic query building for advanced filtering

## 🚀 Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- MySQL 8.0+
- RabbitMQ 3.13+
- Cloudinary account
- Spring Cloud Config Server running on port 8888

### Environment Variables

Create a `.env` file in the root directory:

```properties
# Cloudinary Configuration (Only local configuration needed)
CLOUDINARY_URL=cloudinary://api_key:api_secret@cloud_name
```

**Note**: All other configurations are managed by Spring Cloud Config Server and should be configured there.

### Spring Cloud Config Server

The service requires a Config Server running on `http://localhost:8888`. Configure it as shown in [this repository](https://github.com/semi-conductors/config-server)


### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd user-service
```

2. **Ensure Config Server is running**
```bash
# Config Server should be accessible at http://localhost:8888
# Verify: curl http://localhost:8888/user-service/dev
```

3. **Configure Cloudinary**
```bash
# Create .env file with CLOUDINARY_URL
echo "CLOUDINARY_URL=cloudinary://api_key:api_secret@cloud_name" > .env
```

4. **Build the project**
```bash
mvn clean install
```

5. **Run the service**
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Database Setup

The service uses Flyway for database migrations. The schema will be automatically created on startup from `src/main/resources/db/migration/V1__init_schema.sql`.

## 📚 API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

The complete OpenAPI 3.1 specification is available in `user-api-docs.json` at the root of the repository.

You can view it on GitHub by:
1. Opening the file in the repository
2. GitHub will automatically render the OpenAPI specification

### API Endpoints Overview

#### Authentication
- `POST /users/auth/register` - Register new user
- `POST /users/auth/login` - Login and get tokens
- `POST /users/auth/logout` - Logout and invalidate refresh token
- `POST /users/auth/refresh` - Refresh access token
- `POST /users/auth/password-reset/token` - Request password reset
- `POST /users/auth/password-reset/confirm` - Confirm password reset

#### User Management
- `GET /users/profile` - Get own profile
- `PUT /users/profile` - Update own profile
- `DELETE /users/profile` - Deactivate own account
- `GET /users/{id}` - Get public user profile
- `GET /users/{id}/details` - Get user profile details (Admin/Manager)
- `GET /users` - List all users (Admin/Manager)
- `POST /users` - Create user account (Admin/Manager)
- `PATCH /users/{id}/status` - Update account status (Admin/Manager)
- `PATCH /users/{id}/role` - Update user role (Admin/Manager)

#### Identity Verification
- `GET /users/verifications/upload-urls` - Generate Cloudinary upload URLs
- `GET /users/verifications/can-submit` - Check submission eligibility
- `POST /users/verifications` - Submit verification request
- `GET /users/verifications/my-requests` - Get own verification history
- `GET /users/verifications` - List all verifications (Admin/Manager)
- `GET /users/verifications/{id}` - Get verification details (Admin/Manager)
- `PATCH /users/verifications/{id}/approval` - Approve verification (Admin/Manager)
- `PATCH /users/verifications/{id}/rejection` - Reject verification (Admin/Manager)

#### Ratings & Feedback
- `POST /users/ratings` - Submit rating
- `PUT /users/ratings` - Update rating
- `DELETE /users/ratings/{id}` - Delete rating
- `GET /users/{userId}/ratings` - Get user's received ratings

#### Reports
- `POST /users/reports` - Submit report (Delivery Guy)
- `GET /users/reports/submitted` - Get submitted reports
- `GET /users/reports/received` - Get received reports
- `GET /users/reports` - List all reports (Admin/Manager)
- `GET /users/reports/available` - Get claimable reports (Admin/Manager)
- `GET /users/reports/{id}` - Get report details (Admin/Manager)
- `POST /users/reports/{id}/claim` - Claim report (Admin/Manager)
- `POST /users/reports/{id}/release` - Release report lock (Admin/Manager)
- `POST /users/reports/{id}/refresh-lock` - Refresh lock timer (Admin/Manager)
- `POST /users/reports/{id}/resolve` - Resolve report (Admin/Manager)
- `POST /users/reports/{id}/dismiss` - Dismiss report (Admin/Manager)

## 🗃 Database Schema
```mermaid 
---
config:
  theme: neo-dark
---
erDiagram
	users {
		BIGINT id PK "AUTO_INCREMENT"  
		VARCHAR email  "UNIQUE, NOT NULL"  
		VARCHAR password_hash  "NOT NULL"  
		VARCHAR first_name  "NOT NULL"  
		VARCHAR last_name  "NOT NULL"  
		VARCHAR phone_number  "UNIQUE"  
		TINYINT role  "NOT NULL (1:USER, 2:DELIVERY_GUY, 3:ADMIN, 4:MANAGER)"  
		ENUM account_activity_status  "NOT NULL (ACTIVE, PENDING_REPORT_REVIEW, SUSPENDED_BY_ADMIN)"  
		BOOLEAN is_identity_verified  "NOT NULL DEFAULT FALSE"  
		BOOLEAN is_disabled  "NOT NULL DEFAULT FALSE"  
		BOOLEAN is_email_verified  "NOT NULL DEFAULT FALSE"  
		DECIMAL average_rating  "DEFAULT 0.00 (0-5)"  
		INT total_ratings  "DEFAULT 0"  
		TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
		TIMESTAMP updated_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"  
	}
	verification_requests {
		BIGINT id PK "AUTO_INCREMENT"  
		BIGINT user_id FK "NOT NULL"  
		TEXT id_front_image_url  "NOT NULL"  
		TEXT id_back_image_url  "NOT NULL"  
		VARCHAR id_number  "NULLABLE"  
		ENUM id_type  "NULLABLE (NATIONAL_ID, PASSPORT, DRIVER_LICENSE)"  
		ENUM status  "DEFAULT PENDING (PENDING, APPROVED, REJECTED)"  
		TIMESTAMP submitted_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
		BIGINT reviewed_by FK "NULLABLE"  
		TIMESTAMP reviewed_at  "NULLABLE"  
		TEXT rejection_reason  "NULLABLE"  
		TIMESTAMP can_resubmit_after  "NULLABLE"  
		TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
		TIMESTAMP updated_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"  
	}
	user_ratings {
		BIGINT id PK "AUTO_INCREMENT"  
		BIGINT rater_id FK "NOT NULL"  
		BIGINT rated_user_id FK "NOT NULL"  
		BIGINT rental_request_id  "NOT NULL (Cross-Service Link)"  
		TINYINT rating  "NOT NULL CHECK (rating >= 1 AND rating <= 5)"  
		TEXT feedback  "NULLABLE"  
		BOOLEAN is_visible  "NOT NULL DEFAULT TRUE"  
		TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
		TIMESTAMP updated_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"  
	}
	user_reports {
		BIGINT id PK "AUTO_INCREMENT"  
		BIGINT reporter_user_id FK "NOT NULL"  
		BIGINT reported_user_id FK "NOT NULL"  
		ENUM report_type  "NOT NULL (FRAUD, DAMAGE, OVERDUE, FAKE_USER, THIEVING)"  
		TEXT details  "NOT NULL"  
		JSON evidence_urls  "NULLABLE (Array of image/document URLs)"  
		BIGINT related_rental_id  "NULLABLE (Cross-Service Link)"  
		BIGINT related_delivery_id  "NULLABLE (Cross-Service Link)"  
		ENUM status  "NOT NULL DEFAULT PENDING (PENDING, UNDER_REVIEW, RESOLVED, DISMISSED)"  
		DECIMAL damage_percentage  "NULLABLE (0-100 for DAMAGE reports)"  
		BIGINT assigned_to FK "NULLABLE (Admin assigned to handle)"  
		BIGINT resolved_by FK "NULLABLE"  
		TIMESTAMP resolved_at  "NULLABLE"  
		TEXT resolution_notes  "NULLABLE"  
		BIGINT auto_escalated_from FK "NULLABLE (For OVERDUE -> THIEVING)"  
		TIMESTAMP submitted_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
		TIMESTAMP updated_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"  
	}
	user_sessions {
		BIGINT id PK "AUTO_INCREMENT"  
		BIGINT user_id FK "NOT NULL"  
		VARCHAR token_hash  "NOT NULL, INDEX"  
		TIMESTAMP expires_at  "NOT NULL"  
		BOOLEAN is_active  "NOT NULL DEFAULT TRUE"  
		TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
	}
	password_reset_tokens {
		BIGINT id PK "AUTO_INCREMENT"  
		BIGINT user_id FK "NOT NULL"  
		VARCHAR token_hash  "NOT NULL, INDEX"  
		TIMESTAMP expires_at  "NOT NULL"  
		TIMESTAMP used_at  "NULLABLE"  
		TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"  
	}

	users||--o{verification_requests:"submits"
	users||--o{verification_requests:"reviews (admin)"
	users||--o{user_ratings:"gives rating"
	users||--o{user_ratings:"receives rating"
	users||--o{user_reports:"submits report"
	users||--o{user_reports:"receives report"
	users||--o{user_reports:"assigned to handle"
	users||--o{user_reports:"resolves"
	user_reports||--o{user_reports:"escalates from"
	users||--o{user_sessions:"has sessions"
	users||--o{password_reset_tokens:"requests reset"
```

## 📡 Event Publishing

The service publishes events to RabbitMQ for cross-service communication:

### Published Events

1. **UserRegisteredEvent** (`user.registered`)
    - **Trigger**: New user registration
    - **Payload**:
      ```json
      {
        "userId": 123,
        "username": "John Doe",
        "email": "john@example.com",
        "role": "USER",
        "registeredAt": "2025-01-01T10:00:00"
      }
      ```
    - **Consumers**: Notification Service (welcome email)

2. **PasswordResetRequestedEvent** (`user.password.reset.requested`)
    - **Trigger**: Password reset token requested
    - **Payload**:
      ```json
      {
        "email": "john@example.com",
        "resetToken": "secure-token-hash",
        "expiresAt": "2025-01-01T11:00:00"
      }
      ```
    - **Consumers**: Notification Service (reset email)

3. **ProfileDisabledEvent** (`user.profile.disabled`)
    - **Trigger**: Account disabled (self or admin)
    - **Payload**:
      ```json
      {
        "userId": 123,
        "email": "john@example.com",
        "username": "John Doe",
        "disabledBy": "ADMIN",
        "reason": "Violation of terms",
        "disabledAt": "2025-01-01T10:00:00"
      }
      ```
    - **Consumers**: Notification Service (account status email)

4. **IdentityVerificationApprovedEvent** (`user.identity.verification.approved`)
    - **Trigger**: Identity verification approved by admin
    - **Payload**:
      ```json
      {
        "userId": 123,
        "verificationRequestId": 456,
        "email": "john@example.com",
        "createdAt": "2025-01-01T09:00:00",
        "approvedAt": "2025-01-01T10:00:00"
      }
      ```
    - **Consumers**: Notification Service (approval email)

5. **IdentityVerificationRejectedEvent** (`user.identity.verification.rejected`)
    - **Trigger**: Identity verification rejected by admin
    - **Payload**:
      ```json
      {
        "userId": 123,
        "verificationRequestId": 456,
        "email": "john@example.com",
        "createdAt": "2025-01-01T09:00:00",
        "rejectedAt": "2025-01-01T10:00:00",
        "rejectionReason": "Documents not clear"
      }
      ```
    - **Consumers**: Notification Service (rejection email with reason)

6. **ReportCreatedEvent** (`report.submitted`)
    - **Trigger**: New report submitted
    - **Payload**:
      ```json
      {
        "reportId": 789,
        "reporterId": 123,
        "reportedUserId": 456,
        "reportedUserEmail": "reported@example.com",
        "reportType": "DAMAGE"
      }
      ```
    - **Consumers**: Notification Service (report notification to admins and parties)

### Event Configuration

- **Exchange**: `users.exchange` (TopicExchange)
- **Durable**: Yes (survives broker restarts)
- **Auto-Delete**: No (persistent exchange)
- **Message Format**: JSON (via Jackson2JsonMessageConverter)
- **Error Handling**: AOP-based exception swallowing (non-blocking event publishing)

### Event Publishing Reliability

The service uses aspect-oriented programming to ensure event publishing failures don't block main operations:

```java
@Aspect
public class UserEventPublisherAspect {
    @Around("userEventPublisherMethods()")
    public Object handleAndLogExceptions(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            log.error("Event publishing failed", ex);
            return null; // Non-blocking
        }
    }
}
```

## 🔒 Security

### Authentication Architecture

#### JWT Token Structure
```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user_id",
  "iat": issued_at_timestamp,
  "exp": expiration_timestamp,
  "role": "ROLE_USER",
  "username": "John Doe",
  "email": "john@example.com"
}
```

#### Token Lifecycle

**Access Token**:
- **Lifetime**: 60 minutes (configurable)
- **Storage**: Client-side only (memory/localStorage)
- **Usage**: Sent in `Authorization: Bearer <token>` header
- **Validation**: Signature verified on each request
- **Claims**: User ID, role, username, email

**Refresh Token**:
- **Lifetime**: 30 days (configurable)
- **Storage**: Database (SHA-256 hashed) + client-side
- **Usage**: Exchange for new access token
- **One-Time Use**: Invalidated after refresh
- **Session Tracking**: Linked to `user_sessions` table

#### Authentication Filter Chain

```
SecurityFilterChain:
1. JwtAuthFilter (custom)
   - Extract JWT from Authorization header
   - Validate token signature
   - Extract user claims
   - Set SecurityContext authentication
   
2. UsernamePasswordAuthenticationFilter (default)
   - Handles /login endpoint
   - Delegates to custom AuthenticationProvider
   
3. Authorization filters
   - Check role-based access
   - Enforce method-level security
```

### Custom Authentication Provider

```java
@Component
public class EmailPasswordAuthenticationProvider implements AuthenticationProvider {
    public Authentication authenticate(Authentication auth) {
        // 1. Find user by email
        User user = userRepository.findNotDisabledByEmail(email)
            .orElseThrow(() -> new BadCredentialsException(...));
        
        // 2. Verify password with bcrypt
        if (!passwordEncoder.matches(plainPassword, user.getPasswordHash()))
            throw new BadCredentialsException(...);
        
        // 3. Create authenticated token with role
        return new UsernamePasswordAuthenticationToken(
            user, null, 
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
```

### Password Security

#### Hashing Strategy
- **Algorithm**: Bcrypt with automatic salt generation
- **Strength**: Default bcrypt strength factor (10 rounds)
- **Salt**: Unique per password, embedded in hash
- **Format**: `$2a$10$salt_and_hash_combined`

#### Password Reset Security
1. **Token Generation**: Cryptographically secure random 256-bit token
2. **Token Hashing**: SHA-256 before database storage
3. **Time Limitation**: 60-minute expiration window
4. **One-Time Use**: Marked as used after successful reset
5. **Session Invalidation**: All active sessions terminated on reset
6. **Email Verification**: Only sent if account exists (no user enumeration)

### Role-Based Access Control (RBAC)

#### Implementation

**Method-Level Security**:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public ResponseEntity<UserProfileResponse> updateProfileStatus(...) {
    // Only accessible to ADMIN or MANAGER
}

@PreAuthorize("hasRole('DELIVERY_GUY')")
public ResponseEntity<ReportResponse> createReport(...) {
    // Only accessible to DELIVERY_GUY and higher
}
```

**Dynamic Authorization**:
```java
// Users can only access their own profile unless they're admin
var currentUser = UserService.getAuthenticatedUser();
if (currentUser.getRole().equalsIgnoreCase("USER") && 
    !Objects.equals(id, currentUser.getId())) {
    throw new AccessDeniedException("Not authorized");
}
```

### Security Headers & CORS

**CORS Configuration**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");  // Configurable per environment
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
    return source;
}
```

**Session Management**:
```java
http.sessionManagement(sm -> 
    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
);
// No server-side sessions - fully stateless JWT authentication
```

### Exception Handling

**Custom Authentication Entry Point**:
```java
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) {
        response.setStatus(401);
        response.setContentType("application/json");
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Missing or invalid JWT token"
        );
        // Return RFC 7807 Problem Details
    }
}
```

**Custom Access Denied Handler**:
```java
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) {
        response.setStatus(403);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Insufficient permissions"
        );
        // Return RFC 7807 Problem Details
    }
}
```

### Data Protection

**Sensitive Data Handling**:
- **Passwords**: Never returned in API responses
- **Tokens**: Hashed before storage (SHA-256)
- **Personal Data**: Public profiles expose only necessary information
- **Email Privacy**: Reset requests don't confirm email existence

**SQL Injection Prevention**:
- **Parameterized Queries**: All JPA queries use parameters
- **Specifications**: Dynamic queries built with Criteria API
- **No String Concatenation**: Query parameters always bound safely

**XSS Prevention**:
- **Input Validation**: Jakarta Validation for all DTOs
- **Output Encoding**: JSON serialization handles encoding
- **Content-Type**: Always `application/json` for API responses

## ⏰ Scheduled Tasks

### Report Escalation Scheduler

**Configuration**:
```java
@Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 3)
public void escalateOverdueReports() {
    // Runs every 3 hours
}
```

**Process Flow**:
1. **Query**: Find OVERDUE reports submitted more than 72 hours ago with PENDING status
2. **Create Escalation**:
   ```
   - Generate THIEVING report with detailed escalation notes
   - Include original report details and timeline
   - Provide recommended actions (police report, legal proceedings)
   ```
3. **Update Original**: Mark OVERDUE report as RESOLVED with escalation note
4. **Link Reports**: Set `auto_escalated_from` to create audit trail
5. **Suspend User**:
    - Set `account_activity_status = SUSPENDED_BY_ADMIN`
    - Set `is_disabled = TRUE`
    - Prevent all platform access
6. **Error Handling**: Continue processing even if individual escalation fails

**Escalation Report Template**:
```
AUTO-ESCALATED THEFT REPORT

This report was automatically escalated from OVERDUE (Report ID: {original_id}) 
after 72 hours without resolution.

ORIGINAL OVERDUE REPORT:
Submitted: {original_submit_date}
Details: {original_details}

ESCALATION REASON:
The renter has failed to return the item for more than 3 days after the 
initial overdue report. This constitutes theft.

REQUIRED ACTIONS:
1. Contact local authorities
2. Provide police report with full renter details
3. Initiate legal proceedings if necessary
4. Process insurance claim if applicable
```

### Report Lock Release Scheduler

**Configuration**:
```java
@Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
@Transactional
public void releaseExpiredLocks() {
    // Runs every 30 minutes after previous execution completes
}
```

**Process Flow**:
1. **Query**: `SELECT * FROM user_reports WHERE claimed_by IS NOT NULL AND lock_expires_at < NOW()`
2. **Release Lock**:
   ```java
   report.setClaimedBy(null);
   report.setClaimedAt(null);
   report.setLockExpiresAt(null);
   report.setStatus(ReportStatus.PENDING);
   ```
3. **Log**: Record automatic release for audit
4. **Persist**:Save all changes transactionally

**Benefits**:
- Prevents indefinite locks from admin inactivity
- Ensures abandoned reports return to queue
- Maintains platform responsiveness
- Automatic recovery from admin disconnections

### Scheduler Configuration

**Thread Pool**:
```java
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);  // 5 concurrent scheduled tasks
        scheduler.setThreadNamePrefix("Scheduler-");
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
```

## ⚙️ Configuration

### Spring Cloud Config Integration

**Bootstrap Configuration**:
```yaml
spring:
  application:
    name: user-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true  # Fail startup if config server unavailable
```

**Configuration Hierarchy**:
1. **Config Server**: Centralized configuration (highest priority for external configs)
2. **application.yaml**: Service-specific defaults
3. **.env file**: Local environment overrides (Cloudinary only)
4. **System Properties**: Runtime overrides

### Key Configuration Properties

#### JWT Settings
- `jwt.secret-key`: HS256 signing key (minimum 256 bits recommended)
- `jwt.expiration-time-minutes`: Access token lifetime (default: 60)

#### Session Settings
- `user.session.expiration.time.days`: Refresh token lifetime (default: 30)
- `user.password-reset-token-expiration-minutes`: Reset token lifetime (default: 60)

#### Report Settings
- `report.escalation-period-hours`: Hours before OVERDUE escalates to THIEVING (default: 72)
- `report.locking-period-minutes`: Report claim lock duration (default: 30)

#### Service URLs
- `rental.service.url`: Rental Service endpoint for Feign client (default: http://localhost:8081)

#### Database Settings
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rentmate_users
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate, don't auto-create
    show-sql: false  # Disable in production
    properties:
      hibernate:
        format_sql: true
        jdbc:
          batch_size: 20
```

#### Flyway Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    table: flyway_schema_history
```

#### RabbitMQ Configuration
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
```

### Profile-Specific Configuration

**Development Profile** (`dev`):
- Verbose logging
- H2 console enabled
- Relaxed CORS
- Show SQL queries

**Production Profile** (`prod`):
- Error-level logging only
- Strict CORS
- Connection pooling optimized
- SQL logging disabled

## 📦 Building for Production

### Maven Build

```bash
# Clean build with all tests
mvn clean package

# Skip tests for faster build
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -Pprod
```

## 🔍 Monitoring & Observability

### Spring Boot Actuator

**Enabled Endpoints**:
```
GET /actuator              - List all available endpoints
GET /actuator/health       - Health check status
GET /actuator/info         - Application information
GET /actuator/metrics      - Application metrics
```

**Health Indicators**:
- Database connection status
- RabbitMQ connection status
- Disk space availability
- Custom health checks

### Logging Strategy

**Log Levels**:
- **ERROR**: Critical failures requiring immediate attention
    - Authentication failures
    - Database connection errors
    - Event publishing failures
    - Cross-service communication errors

- **WARN**: Recoverable issues requiring monitoring
    - Expired report locks
    - Failed verification attempts
    - Invalid cross-service references

- **INFO**: Normal operational events
    - User registrations
    - Successful authentications
    - Report escalations
    - Event publications

- **DEBUG**: Detailed debugging information (development only)
    - Request/response details
    - SQL queries
    - Method entry/exit


## 🤝 Integration with Other Services

### Rental Service

**Purpose**: Validates rental transactions for ratings and reports

**Client Configuration**:
```java
@FeignClient(
    name = "rental-service",
    url = "${rental.service.url}"
)
public interface RentalServiceClient {
    @GetMapping("/rentals/{id}")
    RentalResponse getRentalById(@PathVariable Long id);
}
```

**Integration Points**:
1. **Rating Submission**: Verify rental is COMPLETED
2. **Report Validation**: Confirm rental exists and verify parties
3. **Overdue Verification**: Check rental end date has passed

### Notification Service (Async)

**Purpose**: Sends emails and notifications to users

**Communication**: RabbitMQ event-driven

**Event Flow**:
```
User Service → RabbitMQ → Notification Service
             (events)
```

**Consumed Events**:
- `user.registered` → Welcome email
- `user.password.reset.requested` → Password reset email with token
- `user.profile.disabled` → Account suspension notification
- `user.identity.verification.approved` → Verification success email
- `user.identity.verification.rejected` → Rejection email with reason
- `report.submitted` → Report notification to admins and parties

### Cloudinary

**Purpose**: Secure image storage for identity verification documents

**SDK**: Official Cloudinary Java SDK

**Operations**:

1. **Generate Upload URLs**:
```java
public Map<String, Object> generateSignedParams(String publicId) {
    long timestamp = System.currentTimeMillis() / 1000;
    Map<String, Object> params = ObjectUtils.asMap(
        "public_id", publicId,
        "folder", "verifications",
        "timestamp", timestamp
    );
    String signature = cloudinary.apiSignRequest(params, apiSecret);
    params.put("signature", signature);
    params.put("api_key", apiKey);
    return params;
}
```

2. **Validate Image URLs** (Optional):
```java
public boolean isValid(String url) {
    String publicId = extractPublicId(url);
    Map result = cloudinary.api().resource(publicId);
    return result != null && !result.isEmpty();
}
```

**Security**:
- Pre-signed URLs with timestamp and signature
- Time-limited upload capability
- Organized folder structure
- Public read access for verified images

### Spring Cloud Config Server

**Purpose**: Centralized configuration management

**Connection**: REST API (`http://localhost:8888`)

**Configuration Refresh**:
- Restart service to reload configuration
- Use Spring Cloud Bus for dynamic refresh (advanced)

**Fallback**: `fail-fast: true` ensures service won't start without config

## 🐛 Troubleshooting

### Common Issues

**1. JWT Token Expired**
```
Error: 401 Unauthorized - "Token expired"

Solution:
- Use refresh token endpoint: POST /users/auth/refresh
- Implement automatic token refresh in client
- Check token expiration settings in config
```

**2. Config Server Connection Failed**
```
Error: Could not resolve placeholder 'jwt.secret-key'

Solution:
- Verify Config Server is running on port 8888
- Check Config Server URL in application.yaml
- Verify profile name matches Config Server repository
- Use --spring.cloud.config.fail-fast=false for debugging
```

**3. Database Connection Failed**
```
Error: Communications link failure

Solution:
- Verify MySQL is running: systemctl status mysql
- Check database exists: SHOW DATABASES;
- Verify credentials in Config Server
- Check firewall rules for port 3306
```

**4. RabbitMQ Connection Error**
```
Error: Connection refused to localhost:5672

Solution:
- Verify RabbitMQ is running: systemctl status rabbitmq-server
- Check RabbitMQ management console: http://localhost:15672
- Verify credentials (default: guest/guest)
- Check RabbitMQ configuration in Config Server
```

**5. Cloudinary Upload Failed**
```
Error: Invalid signature

Solution:
- Verify CLOUDINARY_URL format in .env file
- Check API key and secret are correct
- Ensure upload happens within time window
- Verify public_id matches pre-signed URL
```

**6. Report Lock Conflicts**
```
Error: Report is locked by another admin

Solution:
- Wait for lock to expire (30 minutes)
- Ask admin to release lock manually
- Use refresh-lock endpoint for long operations
- Check lock_expires_at timestamp
```

**7. Cross-Service Validation Failed**
```
Error: Rental not found or invalid

Solution:
- Verify Rental Service is running
- Check rental ID exists in Rental Service
- Verify Feign client configuration
- Check rental.service.url in Config Server
```

## 📄 License

This project is part of the RentMate platform.


---

For more information, please refer to the [OpenAPI Specification](user-service-openapi.yaml), you can render it at [swagger editor](https://editor.swagger.io/).