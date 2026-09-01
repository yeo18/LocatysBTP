-- ============================================================
-- MIGRATION V5 : Taches structurees d'un TemplateTache
-- ============================================================
-- LOOP INGENIERIE — Template/RBAC (RAPPORT_LOOP_INGENIERIE_TEMPLATE_RBAC.md)
-- Source de verite : MLD cible §4.1 (table template_tache_tache).
--
-- Contexte :
--   Un TemplateTache represente desormais un ENSEMBLE STRUCTURE DE TACHES.
--   Ex. TemplateTache "Préparation dalle" contient : Préparer coffrage,
--   Poser ferraillage, Vérifier niveaux, Couler béton, Faire contrôle.
--
--   La table template_tache_tache porte les taches MODELE (structure),
--   independantes de la table operationnelle `tache` (chantier_id NOT NULL).
--   Aucune FK vers `tache` : l'import reste une copie (snapshot) sans
--   dependance permanente (principe V2 //).
--
-- Contraintes :
--   * ordre unique par template_tache (uk_template_tache_tache_ordre)
--   * titre unique par template_tache (uk_template_tache_tache_titre)
--   * ON DELETE CASCADE : suppression TemplateTache => sous-taches supprimees
--   * conventions identiques a V1/V2 : snake_case, enums VARCHAR + CHECK
-- ============================================================

CREATE TABLE template_tache_tache (
    id                  BIGSERIAL       PRIMARY KEY,
    template_tache_id   BIGINT          NOT NULL,
    titre               VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    priorite            VARCHAR(10)     NOT NULL DEFAULT 'MOYENNE',
    duree_estimee_jours INTEGER,
    ordre               INTEGER         NOT NULL,
    date_creation       TIMESTAMP       NOT NULL,
    date_modification   TIMESTAMP       NOT NULL,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT uk_template_tache_tache_ordre UNIQUE (template_tache_id, ordre),
    CONSTRAINT uk_template_tache_tache_titre UNIQUE (template_tache_id, titre),
    CONSTRAINT ck_template_tache_tache_priorite CHECK (priorite IN ('HAUTE', 'MOYENNE', 'BASSE')),
    CONSTRAINT ck_template_tache_tache_duree CHECK (duree_estimee_jours IS NULL OR duree_estimee_jours > 0),
    CONSTRAINT ck_template_tache_tache_ordre_pos CHECK (ordre >= 1),
    CONSTRAINT fk_template_tache_tache_template FOREIGN KEY (template_tache_id)
        REFERENCES template_tache (id) ON DELETE CASCADE,
    CONSTRAINT fk_template_tache_tache_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_template_tache_tache_template_id ON template_tache_tache (template_tache_id);

-- ============================================================
-- FIN DE LA MIGRATION V5
-- ============================================================