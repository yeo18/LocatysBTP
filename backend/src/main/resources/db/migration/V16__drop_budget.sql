-- ------------------------------------------------------------
-- V16 : suppression des colonnes budget (chantier)
-- et budget_estime (template_chantier)
-- L'utilisateur ne veut plus de budget dans les chantiers.
-- ------------------------------------------------------------

ALTER TABLE chantier DROP COLUMN IF EXISTS budget;

ALTER TABLE template_chantier DROP COLUMN IF EXISTS budget_estime;