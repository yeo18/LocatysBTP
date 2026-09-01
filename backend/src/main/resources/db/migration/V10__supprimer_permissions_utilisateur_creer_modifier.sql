-- ============================================================
-- MIGRATION V10 : suppression des permissions UTILISATEUR_CREER
-- et UTILISATEUR_MODIFIER
-- ============================================================
-- Principe de confidentialité : chaque utilisateur crée (inscription)
-- et modifie lui-même son propre compte. L'admin ne gère pas les
-- identifiants d'autrui → ces deux permissions n'existent plus.
-- ============================================================

DELETE FROM utilisateur_permission WHERE permission_id IN
    (SELECT id FROM permission WHERE code_permission IN ('UTILISATEUR_CREER', 'UTILISATEUR_MODIFIER'));
DELETE FROM profil_permission WHERE permission_id IN
    (SELECT id FROM permission WHERE code_permission IN ('UTILISATEUR_CREER', 'UTILISATEUR_MODIFIER'));
DELETE FROM permission WHERE code_permission IN ('UTILISATEUR_CREER', 'UTILISATEUR_MODIFIER');

-- ============================================================
-- FIN DE LA MIGRATION V10
-- ============================================================
