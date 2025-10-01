alter table `users` modify column account_activity_status
enum('ACTIVE','INACTIVE','PENDING_REPORT_REVIEW', 'SUSPENDED_BY_ADMIN') NOT NULL DEFAULT 'ACTIVE';