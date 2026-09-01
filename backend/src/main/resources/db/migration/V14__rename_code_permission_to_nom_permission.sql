-- ------------------------------------------------------------
-- V14 : renommage de code_permission en nom_permission (permission)
-- Nouvelle denomination plus claire. Renomme aussi la contrainte d'unicite.
-- ------------------------------------------------------------

ALTER TABLE permission RENAME COLUMN code_permission TO nom_permission;

ALTER TABLE permission RENAME CONSTRAINT uk_permission_code_permission TO uk_permission_nom_permission;