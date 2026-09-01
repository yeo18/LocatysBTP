-- ============================================================
-- MIGRATION V21 : Ajout date_creation / date_modification
-- ============================================================
-- Ajoute (avec backfill des lignes existantes) :
--   utilisateur_permission          : date_modification
--   utilisateur_permission_chantier : date_modification
--   affectation_tache_utilisateur   : date_creation + date_modification
--   validation_tache                : date_modification
-- ============================================================

-- utilisateur_permission
ALTER TABLE utilisateur_permission ADD COLUMN date_modification TIMESTAMP;
UPDATE utilisateur_permission SET date_modification = date_creation WHERE date_modification IS NULL;
ALTER TABLE utilisateur_permission ALTER COLUMN date_modification SET NOT NULL;

-- utilisateur_permission_chantier
ALTER TABLE utilisateur_permission_chantier ADD COLUMN date_modification TIMESTAMP;
UPDATE utilisateur_permission_chantier SET date_modification = date_creation WHERE date_modification IS NULL;
ALTER TABLE utilisateur_permission_chantier ALTER COLUMN date_modification SET NOT NULL;

-- affectation_tache_utilisateur
ALTER TABLE affectation_tache_utilisateur ADD COLUMN date_creation TIMESTAMP;
ALTER TABLE affectation_tache_utilisateur ADD COLUMN date_modification TIMESTAMP;
UPDATE affectation_tache_utilisateur
   SET date_creation = COALESCE(date_affectation::timestamp, now()),
       date_modification = COALESCE(date_affectation::timestamp, now())
 WHERE date_creation IS NULL;
ALTER TABLE affectation_tache_utilisateur ALTER COLUMN date_creation SET NOT NULL;
ALTER TABLE affectation_tache_utilisateur ALTER COLUMN date_modification SET NOT NULL;

-- validation_tache
ALTER TABLE validation_tache ADD COLUMN date_modification TIMESTAMP;
UPDATE validation_tache SET date_modification = COALESCE(date_validation::timestamp, now())
 WHERE date_modification IS NULL;
ALTER TABLE validation_tache ALTER COLUMN date_modification SET NOT NULL;

-- ============================================================
-- FIN DE LA MIGRATION V21
-- ============================================================