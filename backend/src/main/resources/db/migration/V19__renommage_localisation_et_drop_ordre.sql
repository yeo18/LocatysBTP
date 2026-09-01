-- ============================================================
-- MIGRATION V19 : Renommage des colonnes de localisation du chantier
--                 + suppression de tache.ordre
-- ============================================================
-- Objectif : rendre les noms de colonnes auto-explicites.
--
--   adresse                  -> adresse_saisie          (adresse tapée par l'utilisateur)
--   localisation_description -> adresse_geocodee        (adresse complète renvoyée par Nominatim)
--   source_localisation      -> origine_coordonnees     (GEOCODAGE | CARTE)
--   precision_localisation   -> fiabilite_coordonnees   (APPROXIMATIVE | PRECISE)
--
--   tache.ordre : supprimé (non utilisé pour l'instant).
-- ============================================================

ALTER TABLE chantier RENAME COLUMN adresse TO adresse_saisie;
ALTER TABLE chantier RENAME COLUMN localisation_description TO adresse_geocodee;
ALTER TABLE chantier RENAME COLUMN source_localisation TO origine_coordonnees;
ALTER TABLE chantier RENAME COLUMN precision_localisation TO fiabilite_coordonnees;

-- Les contraintes CHECK référencent les colonnes par OID : elles continuent de
-- fonctionner après renommage. On renomme leur nom pour garder la cohérence.
ALTER TABLE chantier RENAME CONSTRAINT ck_chantier_source_localisation TO ck_chantier_origine_coordonnees;
ALTER TABLE chantier RENAME CONSTRAINT ck_chantier_precision_localisation TO ck_chantier_fiabilite_coordonnees;

-- Suppression de l'ordre des tâches (non utilisé pour l'instant).
ALTER TABLE tache DROP COLUMN IF EXISTS ordre;

-- ============================================================
-- FIN DE LA MIGRATION V19
-- ============================================================
