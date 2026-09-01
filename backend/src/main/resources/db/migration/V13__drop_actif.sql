-- V13 : suppression du champ `actif` des entites PROFIL, UTILISATEUR et EQUIPE.
-- Decision : la deactivation logique est retiree ; un compte est actif par
-- nature tant qu'il existe. La colonne est supprimee partout (base de donnees).
ALTER TABLE equipe DROP COLUMN IF EXISTS actif;
ALTER TABLE profil DROP COLUMN IF EXISTS actif;
ALTER TABLE utilisateur DROP COLUMN IF EXISTS actif;