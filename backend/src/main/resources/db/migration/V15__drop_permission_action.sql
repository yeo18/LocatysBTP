-- ------------------------------------------------------------
-- V15 : suppression de la colonne action (permission)
-- L'action est deja encodee dans nom_permission (MODULE_ACTION),
-- la colonne etait une denormalisation redondante.
-- ------------------------------------------------------------

ALTER TABLE permission DROP COLUMN action;