-- ============================================================
-- MIGRATION V6 : suppression des modules non conservés
--   * document (documents, pièces jointes, photos de chantier)
--   * planning
--   * commentaire
--
-- Les entités associées ont été retirées du backend (package
-- document, Planning*, Commentaire*). On nettoie le schéma :
-- tables, contraintes, colonnes de rattachement orphelines.
-- ============================================================

-- ---- 1. Colonne orpheline planning_id sur tache (FK vers planning) ----
ALTER TABLE tache DROP CONSTRAINT IF EXISTS fk_tache_planning;
DROP INDEX IF EXISTS ix_tache_planning_id;
ALTER TABLE tache DROP COLUMN IF EXISTS planning_id;

-- ---- 2. Tables des modules supprimés (ordre : dépendances d'abord) ----

-- piece_jointe référence document -> on la supprime en premier
DROP TABLE IF EXISTS piece_jointe;

DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS photo_chantier;
DROP TABLE IF EXISTS commentaire;
DROP TABLE IF EXISTS planning;

-- ============================================================
-- FIN DE LA MIGRATION V6
-- ============================================================
