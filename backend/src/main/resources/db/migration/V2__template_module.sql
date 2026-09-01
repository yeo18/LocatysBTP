-- ============================================================
-- MIGRATION V2 : Module Template (TemplateChantier / TemplateTache)
-- ============================================================
-- LOOP 5.7 — Migration Flyway PostgreSQL du module Template.
-- Source de vérité : MLD-template.md v1.1 + Entity JPA LOOP 5.6
-- (com.cms.template.*).
--
-- Contenu :
--   1. Table template_chantier   — modèle complet de construction.
--   2. Table template_tache      — groupe réutilisable de tâches.
--   3. Table template_chantier_tache — association N:N (porte l'ordre).
--
-- Conventions (identiques à V1) :
--   * Tables/colonnes en snake_case.
--   * Énumérés stockés en VARCHAR + contrainte CHECK.
--   * Contraintes nommées : pk_*, fk_*, uk_*, ck_*, ix_*.
--   * ddl-auto Hibernate = none : aucune création automatique.
--
-- IMPORT : l'import d'un template est une COPIE métier (snapshot).
-- Aucune colonne template_chantier_id dans chantier, aucune colonne
-- template_tache_id dans tache : aucune dépendance permanente entre
-- modèle et données opérationnelles.
-- ============================================================

-- ------------------------------------------------------------
-- TABLE : template_chantier (modèle complet de construction)
-- ------------------------------------------------------------
CREATE TABLE template_chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    nom                 VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    type_construction   VARCHAR(20)     NOT NULL,
    budget_estime       NUMERIC(15, 2),
    duree_estimee_jours INTEGER,
    statut              VARCHAR(10)     NOT NULL DEFAULT 'ACTIF',
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT uk_template_chantier_nom UNIQUE (nom),
    CONSTRAINT ck_template_chantier_type_construction CHECK
        (type_construction IN ('MAISON_R1', 'IMMEUBLE_R5', 'MAGASIN', 'VILLA', 'ENTREPOT')),
    CONSTRAINT ck_template_chantier_statut CHECK (statut IN ('ACTIF', 'INACTIF')),
    CONSTRAINT ck_template_chantier_duree CHECK (duree_estimee_jours IS NULL OR duree_estimee_jours > 0),
    CONSTRAINT fk_template_chantier_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_template_chantier_created_by ON template_chantier (created_by);

-- ------------------------------------------------------------
-- TABLE : template_tache (groupe réutilisable de tâches)
-- ------------------------------------------------------------
CREATE TABLE template_tache (
    id                  BIGSERIAL       PRIMARY KEY,
    titre               VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    priorite            VARCHAR(10)     NOT NULL DEFAULT 'MOYENNE',
    duree_estimee_jours INTEGER,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT ck_template_tache_priorite CHECK (priorite IN ('HAUTE', 'MOYENNE', 'BASSE')),
    CONSTRAINT ck_template_tache_duree CHECK (duree_estimee_jours IS NULL OR duree_estimee_jours > 0),
    CONSTRAINT fk_template_tache_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_template_tache_created_by ON template_tache (created_by);

-- ------------------------------------------------------------
-- TABLE : template_chantier_tache (association N:N, porte l'ordre)
-- ------------------------------------------------------------
CREATE TABLE template_chantier_tache (
    id                  BIGSERIAL       PRIMARY KEY,
    template_chantier_id BIGINT         NOT NULL,
    template_tache_id   BIGINT          NOT NULL,
    ordre               INTEGER         NOT NULL,
    CONSTRAINT uk_template_chantier_tache_ordre UNIQUE (template_chantier_id, ordre),
    CONSTRAINT uk_template_chantier_tache_tache UNIQUE (template_chantier_id, template_tache_id),
    CONSTRAINT ck_template_chantier_tache_ordre CHECK (ordre >= 1),
    CONSTRAINT fk_template_chantier_tache_chantier FOREIGN KEY (template_chantier_id)
        REFERENCES template_chantier (id) ON DELETE CASCADE,
    CONSTRAINT fk_template_chantier_tache_tache FOREIGN KEY (template_tache_id)
        REFERENCES template_tache (id)
);
CREATE INDEX ix_template_chantier_tache_tache_id ON template_chantier_tache (template_tache_id);

-- ============================================================
-- FIN DE LA MIGRATION V2
-- ============================================================
