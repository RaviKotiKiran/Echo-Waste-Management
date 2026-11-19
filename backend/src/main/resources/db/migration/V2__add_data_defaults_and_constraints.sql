ALTER TABLE users
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT chk_users_role CHECK (role IN ('CITIZEN', 'COLLECTOR', 'ADMIN'));

ALTER TABLE waste_requests
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT chk_request_type CHECK (waste_type IN (
        'Household Waste', 'Plastic Waste', 'Paper Waste', 'E-Waste',
        'Garden Waste', 'Metal Waste', 'Organic Waste', 'Construction Waste'
    )),
    ADD CONSTRAINT chk_request_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_request_status CHECK (status IN (
        'PENDING', 'APPROVED', 'ASSIGNED', 'IN_PROGRESS', 'COLLECTED', 'CANCELLED', 'REJECTED'
    ));

ALTER TABLE collector_assignments
    MODIFY assigned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE feedback
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5);

ALTER TABLE service_issues
    MODIFY status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT chk_issue_category CHECK (category IN ('MISSED_PICKUP', 'DAMAGED_BIN', 'ILLEGAL_DUMPING', 'OTHER')),
    ADD CONSTRAINT chk_issue_status CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED'));

ALTER TABLE user_notifications
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT chk_notification_kind CHECK (kind IN ('PICKUP', 'ISSUE'));
