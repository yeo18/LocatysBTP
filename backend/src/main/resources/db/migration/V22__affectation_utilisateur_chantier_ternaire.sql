-- ============================================================
-- MIGRATION V22 : Relation ternaire Utilisateur x Chantier x Profil
-- ============================================================
-- La table affectation_utilisateur_chantier devient ternaire :
--   (utilisateur_id, chantier_id, profil_id) + periode de validite.
-- Regle : sur un chantier, un utilisateur a UN SEUL profil par periode
-- (pas de chevauchement). L'ancienne unicite (utilisateur_id, chantier_id)
-- est retiree pour autoriser plusieurs periodes successives.
-- ============================================================

ALTER TABLE affectation_utilisateur_chantier
    DROP CONSTRAINT uk_affectation_utilisateur_chantier;

ALTER TABLE affectation_utilisateur_chantier
    ADD COLUMN profil_id  BIGINT,
    ADD COLUMN date_debut TIMESTAMP,
    ADD COLUMN date_fin   TIMESTAMP;

-- Les affectations existantes reprennent le profil global de l'utilisateur
-- et la date d'affectation comme debut de periode.
UPDATE affectation_utilisateur_chantier a
    SET profil_id  = (SELECT u.profil_id FROM utilisateur u WHERE u.id = a.utilisateur_id),
        date_debut = a.date_affectation;

ALTER TABLE affectation_utilisateur_chantier
    ALTER COLUMN profil_id  SET NOT NULL,
    ALTER COLUMN date_debut SET NOT NULL;

ALTER TABLE affectation_utilisateur_chantier
    ADD CONSTRAINT fk_affectation_utilisateur_chantier_profil
        FOREIGN KEY (profil_id) REFERENCES profil (id);

CREATE INDEX ix_affectation_utilisateur_chantier_profil_id
    ON affectation_utilisateur_chantier (profil_id);
CREATE INDEX ix_affectation_utilisateur_chantier_utilisateur_chantier
    ON affectation_utilisateur_chantier (utilisateur_id, chantier_id);

-- ============================================================
-- FIN DE LA MIGRATION V22
-- ============================================================