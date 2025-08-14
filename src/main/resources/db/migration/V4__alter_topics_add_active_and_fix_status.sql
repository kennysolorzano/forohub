-- La columna 'active' ya existe en tu BD local. No la vuelvas a crear aquí.

-- Normaliza/asegura valores de 'status' existentes
UPDATE topics
  SET status = 'OPEN'
  WHERE status IS NULL OR status = '';
