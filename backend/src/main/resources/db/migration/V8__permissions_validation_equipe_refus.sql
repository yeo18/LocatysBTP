-- ============================================================
-- MIGRATION V8 : Permissions distinctes « valider » / « refuser »
-- ============================================================
-- STRUCTURE DEMANDEE (utilisateur, 15/08/2026) :
--   * TACHE_VALIDER        -> valider les travaux (profil / tous ceux
--                             à qui la permission est accordée)
--   * TACHE_VALIDER_EQUIPE -> valider les travaux d'une équipe ; accordée
--                             à UNE personne précise d'une équipe (tous les
--                             membres d'une équipe ne peuvent pas valider)
--   * TACHE_REFUSER        -> refuser une validation : si une personne a
--                             validé et que le chef vient vérifier sur le
--                             terrain et que ce n'est pas valide, il refuse.
--
-- TACHE_VALIDER existe déjà (V4). On ajoute les deux autres. L'ADMINISTRATEUR
-- obtient automatiquement toutes les permissions via DroitsService (relecture
-- du référentiel) : aucune ligne profil_permission à insérer ici.
-- ============================================================

INSERT INTO permission (nom, code_permission, module, action, description) VALUES
    ('Valider les travaux d''une équipe', 'TACHE_VALIDER_EQUIPE', 'TACHE', 'VALIDER_EQUIPE', 'Validation des travaux réalisés par une équipe (accordée à une personne désignée)'),
    ('Refuser une validation',             'TACHE_REFUSER',        'TACHE', 'REFUSER',        'Refus d''une validation déjà enregistrée (vérification terrain)');

-- ============================================================
-- FIN DE LA MIGRATION V8
-- ============================================================