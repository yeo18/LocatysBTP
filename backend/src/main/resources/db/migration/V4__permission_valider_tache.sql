-- ============================================================
-- MIGRATION V4 : Permission RBAC « Valider une tâche »
-- ============================================================
-- LOOP RBAC — La validation d'une tâche est désormais une permission
-- distincte (TACHE_VALIDER) accordable ou non à un profil/utilisateur,
-- indépendamment de TACHE_MODIFIER.
--
-- Un utilisateur peut donc :
--   * valider/refuser une tâche  -> TACHE_VALIDER
--   * modifier les champs        -> TACHE_MODIFIER
--
-- Le profil ADMINISTRATEUR obtient automatiquement cette permission via
-- DroitsService (relecture du référentiel en base) : aucune ligne
-- profil_permission à insérer ici. UTILISATEUR_STANDARD n'obtient rien
-- par défaut (moindre privilège) : l'accord se fait via la matrice
-- des habilitations.
-- ============================================================

INSERT INTO permission (nom, code_permission, module, action, description) VALUES
    ('Valider une tâche', 'TACHE_VALIDER', 'TACHE', 'VALIDER', 'Validation ou refus d''une tâche (enregistre une décision)');

-- ============================================================
-- FIN DE LA MIGRATION V4
-- ============================================================