-- V5__uk_topics_title_author.sql
ALTER TABLE topics
  ADD CONSTRAINT uk_topics_title_author UNIQUE (title, author);
