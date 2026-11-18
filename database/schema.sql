-- MySQL 8+ reference schema. The MySQL profile also creates/updates tables with Hibernate.
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(30),
  address VARCHAR(500),
  role VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role (role),
  CONSTRAINT chk_users_role CHECK (role IN ('CITIZEN','COLLECTOR','ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS waste_requests (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  waste_type VARCHAR(40) NOT NULL,
  quantity DECIMAL(10,2) NOT NULL,
  pickup_address VARCHAR(500) NOT NULL,
  pickup_date DATE NOT NULL,
  pickup_time TIME NOT NULL,
  instructions VARCHAR(1000),
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  admin_note VARCHAR(500),
  KEY idx_request_user (user_id),
  KEY idx_request_status (status),
  KEY idx_request_date (pickup_date),
  CONSTRAINT chk_request_type CHECK (waste_type IN ('Household Waste','Plastic Waste','Paper Waste','E-Waste','Garden Waste','Metal Waste','Organic Waste','Construction Waste')),
  CONSTRAINT chk_request_quantity CHECK (quantity > 0),
  CONSTRAINT chk_request_status CHECK (status IN ('PENDING','APPROVED','ASSIGNED','IN_PROGRESS','COLLECTED','CANCELLED','REJECTED')),
  CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS collector_assignments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  request_id BIGINT NOT NULL,
  collector_id BIGINT NOT NULL,
  assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  assignment_status VARCHAR(20) NOT NULL,
  UNIQUE KEY uk_assignment_request (request_id),
  KEY idx_assignment_collector (collector_id),
  CONSTRAINT fk_assignment_request FOREIGN KEY (request_id) REFERENCES waste_requests(id),
  CONSTRAINT fk_assignment_collector FOREIGN KEY (collector_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  request_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comments VARCHAR(1000),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_feedback_request (request_id),
  KEY idx_feedback_user (user_id),
  CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_feedback_request FOREIGN KEY (request_id) REFERENCES waste_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS service_issues (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  request_id BIGINT,
  category VARCHAR(40) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_issue_user_created (user_id, created_at),
  KEY idx_issue_status (status),
  CONSTRAINT chk_issue_category CHECK (category IN ('MISSED_PICKUP','DAMAGED_BIN','ILLEGAL_DUMPING','OTHER')),
  CONSTRAINT chk_issue_status CHECK (status IN ('OPEN','IN_REVIEW','RESOLVED')),
  CONSTRAINT fk_issue_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_issue_request FOREIGN KEY (request_id) REFERENCES waste_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_notifications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  recipient_id BIGINT NOT NULL,
  kind VARCHAR(16) NOT NULL,
  title VARCHAR(120) NOT NULL,
  message VARCHAR(500) NOT NULL,
  link_path VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at DATETIME,
  KEY idx_notification_recipient_created (recipient_id, created_at),
  KEY idx_notification_recipient_read (recipient_id, read_at),
  CONSTRAINT chk_notification_kind CHECK (kind IN ('PICKUP','ISSUE')),
  CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
