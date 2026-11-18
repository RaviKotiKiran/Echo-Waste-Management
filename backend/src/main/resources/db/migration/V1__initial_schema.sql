CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE waste_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    waste_type VARCHAR(40) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    pickup_date DATE NOT NULL,
    pickup_time TIME(6) NOT NULL,
    instructions VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    admin_note VARCHAR(500),
    PRIMARY KEY (id),
    INDEX idx_request_user (user_id),
    INDEX idx_request_status (status),
    INDEX idx_request_date (pickup_date),
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE collector_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    collector_id BIGINT NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    assignment_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_assignment_request UNIQUE (request_id),
    INDEX idx_assignment_collector (collector_id),
    CONSTRAINT fk_assignment_request FOREIGN KEY (request_id) REFERENCES waste_requests (id),
    CONSTRAINT fk_assignment_collector FOREIGN KEY (collector_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comments VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_feedback_request UNIQUE (request_id),
    INDEX idx_feedback_user (user_id),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_feedback_request FOREIGN KEY (request_id) REFERENCES waste_requests (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_issues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_id BIGINT,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_issue_user_created (user_id, created_at),
    INDEX idx_issue_status (status),
    CONSTRAINT fk_issue_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_issue_request FOREIGN KEY (request_id) REFERENCES waste_requests (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    kind VARCHAR(16) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    link_path VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_notification_recipient_created (recipient_id, created_at),
    INDEX idx_notification_recipient_read (recipient_id, read_at),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
