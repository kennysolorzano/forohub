CREATE TABLE topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  message TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  author VARCHAR(120) NOT NULL,
  course VARCHAR(120) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT uk_topics_title_message UNIQUE (title, message)
);

CREATE INDEX idx_topics_active ON topics(active);
CREATE INDEX idx_topics_created_at ON topics(created_at);
CREATE INDEX idx_topics_course ON topics(course);
