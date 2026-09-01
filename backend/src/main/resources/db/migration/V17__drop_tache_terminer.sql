-- ------------------------------------------------------------
-- V17 : suppression du statut TERMINE de la tache
-- Le statut VALIDE couvre deja le fait d'une tache terminee.
-- La contrainte de la tache est recreee sans TERMINE.
-- Termine pour les chantiers (ChantierStatut.TERMINE) est conserve.
-- ------------------------------------------------------------

ALTER TABLE tache DROP CONSTRAINT IF EXISTS ck_tache_status;

ALTER TABLE tache ADD CONSTRAINT ck_tache_status
    CHECK (status IN ('A_FAIRE', 'EN_COURS', 'VALIDE', 'REFUSE'));