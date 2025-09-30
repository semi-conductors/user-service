-- Set the default character set and collation
SET NAMES utf8mb4;

-- -----------------------------------------------------
-- Table structure for `users`
-- -----------------------------------------------------
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    
    -- Role Hierarchy: 1:USER, 2:DELIVERY_GUY, 3:ADMIN, 4:MANAGER
    role TINYINT NOT NULL,
    
    -- Account Status and Flags
    account_activity_status ENUM('ACTIVE', 'PENDING_REPORT_REVIEW', 'SUSPENDED_BY_ADMIN') NOT NULL DEFAULT 'ACTIVE',
    is_identity_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_number_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Denormalized Ratings
    average_rating DECIMAL(5, 2) DEFAULT 0.00,
    total_ratings INT DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- -----------------------------------------------------
-- Table structure for `verification_requests`
-- -----------------------------------------------------
CREATE TABLE verification_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    id_front_image_url TEXT NOT NULL,
    id_back_image_url TEXT NOT NULL,
    id_number VARCHAR(50) NULL,
    
    -- Review Status
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Reviewer Details
    reviewed_by BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    rejection_reason TEXT NULL,
    can_resubmit_after TIMESTAMP NULL,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),

    -- Foreign Keys
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- -----------------------------------------------------
-- Table structure for `user_ratings`
-- -----------------------------------------------------
CREATE TABLE user_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rater_id BIGINT NOT NULL,
    rated_user_id BIGINT NOT NULL,
    rental_request_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    feedback TEXT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Constraints
    UNIQUE KEY uk_rental_rating (rental_request_id, rater_id), -- Ensures one rating per rental per user
    CONSTRAINT chk_rating_range CHECK (rating >= 0 AND rating <= 5),
    
    -- Foreign Keys
    CONSTRAINT fk_rating_rater FOREIGN KEY (rater_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rated FOREIGN KEY (rated_user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- -----------------------------------------------------
-- Table structure for `user_reports`
-- -----------------------------------------------------
CREATE TABLE user_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_user_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    report_type ENUM('FRAUD', 'DAMAGE', 'OVERDUE', 'FAKE_USER', 'THIEVING') NOT NULL,
    details TEXT NOT NULL,
    evidence_urls JSON NULL, -- Stores array of URLs for photos/videos
    
    -- Cross-Service Links
    related_rental_id BIGINT NULL,
    related_delivery_id BIGINT NULL,
    
    -- Admin Management Fields
    status ENUM('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'PENDING',
    damage_percentage DECIMAL(5, 2) NULL,
    assigned_to BIGINT NULL,
    resolved_by BIGINT NULL,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT NULL,
    
    -- Escalation Tracking
    auto_escalated_from BIGINT NULL,
    
    -- Timestamps
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Foreign Keys
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_report_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_report_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_report_escalation FOREIGN KEY (auto_escalated_from) REFERENCES user_reports (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- -----------------------------------------------------
-- Table structure for `user_sessions` (for Refresh Tokens)
-- -----------------------------------------------------
CREATE TABLE user_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Indexes and Foreign Keys
    INDEX idx_token_hash (token_hash),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- -----------------------------------------------------
-- Table structure for `password_reset_tokens`
-- -----------------------------------------------------
CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Indexes and Foreign Keys
    INDEX idx_reset_token (token_hash),
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;