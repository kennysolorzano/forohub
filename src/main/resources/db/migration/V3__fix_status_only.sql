ALTER TABLE topics
  MODIFY COLUMN status VARCHAR(20) NOT NULL;

UPDATE topics
  SET status = 'OPEN'
  WHERE status IS NULL OR status = '';
