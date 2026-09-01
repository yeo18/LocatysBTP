-- ============================================================
-- MIGRATION V7 : suppression du module audit
--   * historique_action (journal d'audit métier)
--   * journal_connexion (trace des connexions)
--
-- Le module audit a été retiré du backend (package com.cms.audit).
-- L'utilisateur recréera ces tables le jour où il en aura besoin.
-- Idempotente : sûre même si la base a déjà été nettoyée.
-- ============================================================

DROP TABLE IF EXISTS historique_action;
DROP TABLE IF EXISTS journal_connexion;

-- ============================================================
-- FIN DE LA MIGRATION V7
-- ============================================================
