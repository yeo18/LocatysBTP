-- ============================================================
-- MIGRATION V1 : Schéma complet CMS + seed RBAC système
-- ============================================================
-- LOOP 3.17 — Migration Flyway PostgreSQL définitive.
-- Source de vérité : entités JPA (com.cms.*) + MLD relationnel
-- (docs/MLD-relationnel.md v1.0).
--
-- Contenu :
--   1. Création des 19 tables métier (PK, FK, UNIQUE, CHECK, index).
--   2. Seed RBAC : profils système ADMINISTRATEUR / UTILISATEUR_STANDARD
--      + permissions validées (modules existants uniquement :
--      UTILISATEUR, EQUIPE, CHANTIER, TACHE, PERMISSION, PROFIL).
--
-- Conventions :
--   * Tables/colonnes en snake_case.
--   * Énumérés stockés en VARCHAR + contrainte CHECK (cf. MLD §5.4).
--   * Contraintes nommées : pk_*, fk_*, uk_*, ck_*, ix_*.
--   * ddl-auto Hibernate = none : aucune création automatique.
-- ============================================================

-- ============================================================
-- 1. TABLES MÉTIER
-- ============================================================

-- ------------------------------------------------------------
-- TABLE : profil (RBAC — rôle métier portant des droits)
-- ------------------------------------------------------------
CREATE TABLE profil (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),
    actif               BOOLEAN         NOT NULL DEFAULT TRUE,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    CONSTRAINT uk_profil_nom UNIQUE (nom)
);

-- ------------------------------------------------------------
-- TABLE : permission (RBAC — unité de droit, code MODULE_ACTION)
-- ------------------------------------------------------------
CREATE TABLE permission (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    code_permission     VARCHAR(100)    NOT NULL,
    module              VARCHAR(50)     NOT NULL,
    action              VARCHAR(50)     NOT NULL,
    description         VARCHAR(255),
    CONSTRAINT uk_permission_code_permission UNIQUE (code_permission)
);

-- ------------------------------------------------------------
-- TABLE : utilisateur (compte de connexion, base de l'auth)
-- ------------------------------------------------------------
CREATE TABLE utilisateur (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    prenom              VARCHAR(100)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    telephone           VARCHAR(20),
    actif               BOOLEAN         NOT NULL DEFAULT TRUE,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    profil_id           BIGINT          NOT NULL,
    CONSTRAINT uk_utilisateur_email UNIQUE (email),
    CONSTRAINT fk_utilisateur_profil FOREIGN KEY (profil_id)
        REFERENCES profil (id)
);
CREATE INDEX ix_utilisateur_profil_id ON utilisateur (profil_id);

-- ------------------------------------------------------------
-- TABLE : profil_permission (attribution de permissions à un profil)
-- ------------------------------------------------------------
CREATE TABLE profil_permission (
    id                  BIGSERIAL       PRIMARY KEY,
    profil_id           BIGINT          NOT NULL,
    permission_id       BIGINT          NOT NULL,
    CONSTRAINT uk_profil_permission UNIQUE (profil_id, permission_id),
    CONSTRAINT fk_profil_permission_profil FOREIGN KEY (profil_id)
        REFERENCES profil (id),
    CONSTRAINT fk_profil_permission_permission FOREIGN KEY (permission_id)
        REFERENCES permission (id)
);
CREATE INDEX ix_profil_permission_permission_id ON profil_permission (permission_id);

-- ------------------------------------------------------------
-- TABLE : utilisateur_permission (exceptions RBAC individuelles)
-- ------------------------------------------------------------
CREATE TABLE utilisateur_permission (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    permission_id       BIGINT          NOT NULL,
    type                VARCHAR(10)     NOT NULL,
    date_creation       TIMESTAMP       NOT NULL,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT uk_utilisateur_permission UNIQUE (utilisateur_id, permission_id, type),
    CONSTRAINT ck_utilisateur_permission_type CHECK (type IN ('ACCORDER', 'REFUSER')),
    CONSTRAINT fk_utilisateur_permission_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_utilisateur_permission_permission FOREIGN KEY (permission_id)
        REFERENCES permission (id),
    CONSTRAINT fk_utilisateur_permission_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_utilisateur_permission_permission_id ON utilisateur_permission (permission_id);
CREATE INDEX ix_utilisateur_permission_created_by ON utilisateur_permission (created_by);

-- ------------------------------------------------------------
-- TABLE : chantier (entité centrale : projet de construction)
-- ------------------------------------------------------------
CREATE TABLE chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    adresse             VARCHAR(255),
    budget              NUMERIC(15, 2),
    statut              VARCHAR(20)     NOT NULL DEFAULT 'PREVU',
    date_debut          DATE,
    date_fin            DATE,
    progression         INTEGER         NOT NULL DEFAULT 0,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    responsable_id      BIGINT,
    CONSTRAINT ck_chantier_statut CHECK (statut IN ('PREVU', 'EN_COURS', 'TERMINE', 'ANNULE')),
    CONSTRAINT ck_chantier_progression CHECK (progression BETWEEN 0 AND 100),
    CONSTRAINT ck_chantier_dates CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut),
    CONSTRAINT fk_chantier_responsable FOREIGN KEY (responsable_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_chantier_responsable_id ON chantier (responsable_id);

-- ------------------------------------------------------------
-- TABLE : equipe (groupe de travail mobile)
-- ------------------------------------------------------------
CREATE TABLE equipe (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),
    actif               BOOLEAN         NOT NULL DEFAULT TRUE,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL
);

-- ------------------------------------------------------------
-- TABLE : membre_equipe (appartenance d'un utilisateur à une équipe)
-- ------------------------------------------------------------
CREATE TABLE membre_equipe (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    equipe_id           BIGINT          NOT NULL,
    role_dans_equipe    VARCHAR(10)     NOT NULL,
    date_integration    DATE            NOT NULL,
    CONSTRAINT uk_membre_equipe UNIQUE (utilisateur_id, equipe_id),
    CONSTRAINT ck_membre_equipe_role CHECK (role_dans_equipe IN ('CHEF', 'OUVRIER')),
    CONSTRAINT fk_membre_equipe_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_membre_equipe_equipe FOREIGN KEY (equipe_id)
        REFERENCES equipe (id)
);
CREATE INDEX ix_membre_equipe_equipe_id ON membre_equipe (equipe_id);

-- ------------------------------------------------------------
-- TABLE : affectation_equipe_chantier (affectation d'une équipe
-- à un chantier par période — base de la sécurité par périmètre)
-- ------------------------------------------------------------
CREATE TABLE affectation_equipe_chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    equipe_id           BIGINT          NOT NULL,
    chantier_id         BIGINT          NOT NULL,
    date_debut          DATE            NOT NULL,
    date_fin            DATE,
    statut              VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT ck_affectation_equipe_chantier_statut CHECK (statut IN ('ACTIVE', 'TERMINEE')),
    CONSTRAINT ck_affectation_equipe_chantier_dates CHECK (date_fin IS NULL OR date_fin >= date_debut),
    CONSTRAINT fk_affectation_equipe_chantier_equipe FOREIGN KEY (equipe_id)
        REFERENCES equipe (id),
    CONSTRAINT fk_affectation_equipe_chantier_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id)
);
CREATE INDEX ix_affectation_equipe_chantier_chantier_id ON affectation_equipe_chantier (chantier_id);
CREATE INDEX ix_affectation_equipe_chantier_equipe_id ON affectation_equipe_chantier (equipe_id);

-- ------------------------------------------------------------
-- TABLE : planning (période de planification d'un chantier)
-- ------------------------------------------------------------
CREATE TABLE planning (
    id                  BIGSERIAL       PRIMARY KEY,
    chantier_id         BIGINT          NOT NULL,
    libelle             VARCHAR(100)    NOT NULL,
    type                VARCHAR(15)     NOT NULL,
    date_debut          DATE            NOT NULL,
    date_fin            DATE,
    statut              VARCHAR(10)     NOT NULL DEFAULT 'PREVU',
    date_creation       TIMESTAMP       NOT NULL,
    CONSTRAINT ck_planning_type CHECK (type IN ('GLOBAL', 'HEBDOMADAIRE', 'MENSUEL')),
    CONSTRAINT ck_planning_statut CHECK (statut IN ('PREVU', 'ACTIF', 'TERMINE')),
    CONSTRAINT ck_planning_dates CHECK (date_fin IS NULL OR date_fin >= date_debut),
    CONSTRAINT fk_planning_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id)
);
CREATE INDEX ix_planning_chantier_id ON planning (chantier_id);

-- ------------------------------------------------------------
-- TABLE : tache (unité de travail d'un chantier)
-- ------------------------------------------------------------
CREATE TABLE tache (
    id                  BIGSERIAL       PRIMARY KEY,
    titre               VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    priorite            VARCHAR(10)     NOT NULL DEFAULT 'MOYENNE',
    status              VARCHAR(10)     NOT NULL DEFAULT 'A_FAIRE',
    ordre               INTEGER,
    progression         INTEGER         NOT NULL DEFAULT 0,
    date_debut          DATE,
    date_fin            DATE,
    date_realisation    DATE,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    chantier_id         BIGINT          NOT NULL,
    planning_id         BIGINT,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT ck_tache_priorite CHECK (priorite IN ('HAUTE', 'MOYENNE', 'BASSE')),
    CONSTRAINT ck_tache_status CHECK (status IN ('A_FAIRE', 'EN_COURS', 'TERMINE', 'VALIDE', 'REFUSE')),
    CONSTRAINT ck_tache_progression CHECK (progression BETWEEN 0 AND 100),
    CONSTRAINT ck_tache_dates CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut),
    CONSTRAINT fk_tache_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id),
    CONSTRAINT fk_tache_planning FOREIGN KEY (planning_id)
        REFERENCES planning (id),
    CONSTRAINT fk_tache_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_tache_chantier_id ON tache (chantier_id);
CREATE INDEX ix_tache_planning_id ON tache (planning_id);
CREATE INDEX ix_tache_created_by ON tache (created_by);

-- ------------------------------------------------------------
-- TABLE : affectation_tache (affectation d'une tâche à un
-- utilisateur OU une équipe)
-- ------------------------------------------------------------
CREATE TABLE affectation_tache (
    id                  BIGSERIAL       PRIMARY KEY,
    tache_id            BIGINT          NOT NULL,
    utilisateur_id      BIGINT,
    equipe_id           BIGINT,
    role                VARCHAR(15)     NOT NULL,
    date_affectation    DATE            NOT NULL,
    CONSTRAINT ck_affectation_tache_role CHECK (role IN ('REALISATEUR', 'CONTROLEUR')),
    CONSTRAINT ck_affectation_tache_cible CHECK (utilisateur_id IS NOT NULL OR equipe_id IS NOT NULL),
    CONSTRAINT fk_affectation_tache_tache FOREIGN KEY (tache_id)
        REFERENCES tache (id),
    CONSTRAINT fk_affectation_tache_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_affectation_tache_equipe FOREIGN KEY (equipe_id)
        REFERENCES equipe (id)
);
CREATE INDEX ix_affectation_tache_tache_id ON affectation_tache (tache_id);
CREATE INDEX ix_affectation_tache_utilisateur_id ON affectation_tache (utilisateur_id);
CREATE INDEX ix_affectation_tache_equipe_id ON affectation_tache (equipe_id);

-- ------------------------------------------------------------
-- TABLE : validation_tache (historique des décisions de validation)
-- ------------------------------------------------------------
CREATE TABLE validation_tache (
    id                  BIGSERIAL       PRIMARY KEY,
    tache_id            BIGINT          NOT NULL,
    validateur_id       BIGINT          NOT NULL,
    statut              VARCHAR(10)     NOT NULL,
    commentaire         VARCHAR(500),
    date_validation     DATE            NOT NULL,
    CONSTRAINT ck_validation_tache_statut CHECK (statut IN ('VALIDE', 'REFUSE')),
    CONSTRAINT fk_validation_tache_tache FOREIGN KEY (tache_id)
        REFERENCES tache (id),
    CONSTRAINT fk_validation_tache_validateur FOREIGN KEY (validateur_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_validation_tache_tache_id ON validation_tache (tache_id);
CREATE INDEX ix_validation_tache_validateur_id ON validation_tache (validateur_id);

-- ------------------------------------------------------------
-- TABLE : commentaire (tâche OU chantier — un seul contexte)
-- ------------------------------------------------------------
CREATE TABLE commentaire (
    id                  BIGSERIAL       PRIMARY KEY,
    auteur_id           BIGINT          NOT NULL,
    tache_id            BIGINT,
    chantier_id         BIGINT,
    contenu             VARCHAR(1000)   NOT NULL,
    date_creation       TIMESTAMP       NOT NULL,
    CONSTRAINT ck_commentaire_reference CHECK (tache_id IS NOT NULL OR chantier_id IS NOT NULL),
    CONSTRAINT fk_commentaire_auteur FOREIGN KEY (auteur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_commentaire_tache FOREIGN KEY (tache_id)
        REFERENCES tache (id),
    CONSTRAINT fk_commentaire_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id)
);
CREATE INDEX ix_commentaire_auteur_id ON commentaire (auteur_id);
CREATE INDEX ix_commentaire_tache_id ON commentaire (tache_id);
CREATE INDEX ix_commentaire_chantier_id ON commentaire (chantier_id);

-- ------------------------------------------------------------
-- TABLE : historique_action (journal d'audit — immuable)
-- ------------------------------------------------------------
CREATE TABLE historique_action (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    action              VARCHAR(100)    NOT NULL,
    type_entite         VARCHAR(100)    NOT NULL,
    entite_id           BIGINT          NOT NULL,
    details             VARCHAR(2000),
    ip_adresse          VARCHAR(45),
    date_action         TIMESTAMP       NOT NULL,
    CONSTRAINT fk_historique_action_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_historique_action_utilisateur_id ON historique_action (utilisateur_id);

-- ------------------------------------------------------------
-- TABLE : journal_connexion (trace des connexions/déconnexions)
-- ------------------------------------------------------------
CREATE TABLE journal_connexion (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    type                VARCHAR(10)     NOT NULL,
    statut              VARCHAR(10)     NOT NULL,
    ip_adresse          VARCHAR(45),
    user_agent          VARCHAR(255),
    date_connexion      TIMESTAMP       NOT NULL,
    CONSTRAINT ck_journal_connexion_type CHECK (type IN ('LOGIN', 'LOGOUT')),
    CONSTRAINT ck_journal_connexion_statut CHECK (statut IN ('SUCCES', 'ECHEC')),
    CONSTRAINT fk_journal_connexion_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_journal_connexion_utilisateur_id ON journal_connexion (utilisateur_id);

-- ------------------------------------------------------------
-- TABLE : document (document métier d'un chantier)
-- ------------------------------------------------------------
CREATE TABLE document (
    id                  BIGSERIAL       PRIMARY KEY,
    titre               VARCHAR(255)    NOT NULL,
    type                VARCHAR(20)     NOT NULL,
    chantier_id         BIGINT          NOT NULL,
    uploader_id         BIGINT          NOT NULL,
    chemin_fichier      VARCHAR(500)    NOT NULL,
    extension           VARCHAR(10),
    taille              BIGINT,
    statut              VARCHAR(10)     NOT NULL DEFAULT 'BROUILLON',
    date_upload         TIMESTAMP       NOT NULL,
    CONSTRAINT ck_document_type CHECK (type IN ('CONTRAT', 'PLAN', 'DEVIS', 'RAPPORT', 'FACTURE', 'AUTRE')),
    CONSTRAINT ck_document_statut CHECK (statut IN ('BROUILLON', 'FINAL')),
    CONSTRAINT fk_document_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id),
    CONSTRAINT fk_document_uploader FOREIGN KEY (uploader_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_document_chantier_id ON document (chantier_id);
CREATE INDEX ix_document_uploader_id ON document (uploader_id);

-- ------------------------------------------------------------
-- TABLE : piece_jointe (fichier associé ; rattachement au document
-- et/ou cible polymorphe TACHE/COMMENTAIRE/VALIDATION)
-- ------------------------------------------------------------
CREATE TABLE piece_jointe (
    id                  BIGSERIAL       PRIMARY KEY,
    document_id         BIGINT,
    entite_type         VARCHAR(15),
    entite_id           BIGINT,
    chemin_fichier      VARCHAR(500)    NOT NULL,
    nom_original        VARCHAR(255)    NOT NULL,
    extension           VARCHAR(10),
    taille              BIGINT,
    date_ajout          TIMESTAMP       NOT NULL,
    CONSTRAINT ck_piece_jointe_entite_type CHECK (entite_type IN ('TACHE', 'COMMENTAIRE', 'VALIDATION')),
    CONSTRAINT fk_piece_jointe_document FOREIGN KEY (document_id)
        REFERENCES document (id)
);
CREATE INDEX ix_piece_jointe_document_id ON piece_jointe (document_id);

-- ------------------------------------------------------------
-- TABLE : photo_chantier (photo d'avancement d'un chantier)
-- ------------------------------------------------------------
CREATE TABLE photo_chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    chantier_id         BIGINT          NOT NULL,
    utilisateur_id      BIGINT          NOT NULL,
    chemin              VARCHAR(500)    NOT NULL,
    description         VARCHAR(255),
    type                VARCHAR(10)     NOT NULL,
    date_prise          DATE            NOT NULL,
    CONSTRAINT ck_photo_chantier_type CHECK (type IN ('AVANT', 'PENDANT', 'APRES')),
    CONSTRAINT fk_photo_chantier_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id),
    CONSTRAINT fk_photo_chantier_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_photo_chantier_chantier_id ON photo_chantier (chantier_id);
CREATE INDEX ix_photo_chantier_utilisateur_id ON photo_chantier (utilisateur_id);

-- ============================================================
-- 2. SEED RBAC SYSTÈME
-- ============================================================

-- ------------------------------------------------------------
-- Permissions validées (modules existants uniquement)
-- Format du code : MODULE_ACTION (MAJUSCULES, cf. rbac-dynamique §3).
-- ------------------------------------------------------------
INSERT INTO permission (nom, code_permission, module, action, description) VALUES
    ('Créer un utilisateur',     'UTILISATEUR_CREER',     'UTILISATEUR', 'CREER',     'Création d''un compte utilisateur'),
    ('Lire un utilisateur',      'UTILISATEUR_LIRE',      'UTILISATEUR', 'LIRE',      'Consultation des comptes utilisateurs'),
    ('Modifier un utilisateur',  'UTILISATEUR_MODIFIER',  'UTILISATEUR', 'MODIFIER',  'Modification d''un compte utilisateur'),
    ('Supprimer un utilisateur', 'UTILISATEUR_SUPPRIMER', 'UTILISATEUR', 'SUPPRIMER', 'Suppression d''un compte utilisateur'),
    ('Créer une équipe',         'EQUIPE_CREER',          'EQUIPE',      'CREER',     'Création d''une équipe'),
    ('Lire une équipe',          'EQUIPE_LIRE',           'EQUIPE',      'LIRE',      'Consultation des équipes'),
    ('Modifier une équipe',      'EQUIPE_MODIFIER',       'EQUIPE',      'MODIFIER',  'Modification d''une équipe'),
    ('Supprimer une équipe',     'EQUIPE_SUPPRIMER',      'EQUIPE',      'SUPPRIMER', 'Suppression d''une équipe'),
    ('Créer un chantier',        'CHANTIER_CREER',        'CHANTIER',    'CREER',     'Création d''un chantier'),
    ('Lire un chantier',         'CHANTIER_LIRE',         'CHANTIER',    'LIRE',      'Consultation des chantiers'),
    ('Modifier un chantier',     'CHANTIER_MODIFIER',     'CHANTIER',    'MODIFIER',  'Modification d''un chantier'),
    ('Supprimer un chantier',    'CHANTIER_SUPPRIMER',    'CHANTIER',    'SUPPRIMER', 'Suppression d''un chantier'),
    ('Créer une tâche',          'TACHE_CREER',           'TACHE',       'CREER',     'Création d''une tâche'),
    ('Lire une tâche',           'TACHE_LIRE',            'TACHE',       'LIRE',      'Consultation des tâches'),
    ('Modifier une tâche',       'TACHE_MODIFIER',        'TACHE',       'MODIFIER',  'Modification d''une tâche'),
    ('Supprimer une tâche',      'TACHE_SUPPRIMER',       'TACHE',       'SUPPRIMER', 'Suppression d''une tâche'),
    ('Créer une permission',     'PERMISSION_CREER',      'PERMISSION',  'CREER',     'Création d''une permission'),
    ('Lire une permission',      'PERMISSION_LIRE',       'PERMISSION',  'LIRE',      'Consultation des permissions'),
    ('Modifier une permission',  'PERMISSION_MODIFIER',   'PERMISSION',  'MODIFIER',  'Modification d''une permission'),
    ('Supprimer une permission', 'PERMISSION_SUPPRIMER',  'PERMISSION',  'SUPPRIMER', 'Suppression d''une permission'),
    ('Créer un profil',          'PROFIL_CREER',          'PROFIL',      'CREER',     'Création d''un profil'),
    ('Lire un profil',           'PROFIL_LIRE',           'PROFIL',      'LIRE',      'Consultation des profils'),
    ('Modifier un profil',       'PROFIL_MODIFIER',       'PROFIL',      'MODIFIER',  'Modification d''un profil'),
    ('Supprimer un profil',      'PROFIL_SUPPRIMER',      'PROFIL',      'SUPPRIMER', 'Suppression d''un profil');

-- ------------------------------------------------------------
-- Profils système (non supprimables, non renommables)
-- ------------------------------------------------------------
INSERT INTO profil (nom, description, actif, date_creation, date_modification) VALUES
    ('ADMINISTRATEUR', 'Profil système : gestion complète de l''application', TRUE, NOW(), NOW()),
    ('UTILISATEUR_STANDARD', 'Profil système : accès limité selon ses permissions', TRUE, NOW(), NOW());

-- ------------------------------------------------------------
-- Droits par défaut :
--   * ADMINISTRATEUR        : ensemble des permissions du référentiel
--                             (relues en base par DroitsService — jamais
--                             en dur, cf. rbac-dynamique §2) ; aucune
--                             ligne profil_permission requise.
--   * UTILISATEUR_STANDARD  : permissions de consultation de base.
-- ------------------------------------------------------------
INSERT INTO profil_permission (profil_id, permission_id)
SELECT p.id, perm.id
FROM profil p
JOIN permission perm ON perm.code_permission IN
    ('UTILISATEUR_LIRE', 'EQUIPE_LIRE', 'CHANTIER_LIRE', 'TACHE_LIRE')
WHERE p.nom = 'UTILISATEUR_STANDARD';

-- ============================================================
-- FIN DE LA MIGRATION V1
-- ============================================================
