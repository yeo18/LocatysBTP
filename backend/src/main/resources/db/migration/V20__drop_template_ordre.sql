-- ============================================================
-- MIGRATION V20 : Suppression de l'ordre des templates
-- ============================================================
-- Supprime l'ordre d'exécution des tâches de template (non utilisé
-- pour l'instant). Les contraintes UNIQUE/CHECK liées à ces colonnes
-- sont supprimées automatiquement par le DROP COLUMN.
-- ============================================================

ALTER TABLE template_tache_tache DROP COLUMN IF EXISTS ordre;
ALTER TABLE template_chantier_tache DROP COLUMN IF EXISTS ordre;

-- ============================================================
-- FIN DE LA MIGRATION V20
-- ============================================================
