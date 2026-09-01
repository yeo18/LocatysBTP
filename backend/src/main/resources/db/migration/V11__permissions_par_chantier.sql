-- ============================================================
-- MIGRATION V11 : Permissions scopées par chantier
-- ============================================================
-- Permet d'accorder une permission à un utilisateur POUR UN
-- CHANTIER PRÉCIS (« chef du chantier A, pas du chantier B »).
--
-- UtilisateurPermissionChantier = exception RBAC (ACCORDER /
-- REFUSER) liée à un chantier. Elle s'ajoute (ACCORDER) ou retranche
-- (REFUSER, prioritaire) les droits du profil dans le périmètre de CE
-- chantier uniquement. Une exception globale (utilisateur_permission,
-- sans chantier) reste valable sur tous les chantiers.
-- ============================================================

CREATE TABLE utilisateur_permission_chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    chantier_id         BIGINT          NOT NULL,
    permission_id       BIGINT          NOT NULL,
    type                VARCHAR(10)     NOT NULL,
    date_creation       TIMESTAMP       NOT NULL,
    created_by          BIGINT          NOT NULL,
    CONSTRAINT uk_utilisateur_permission_chantier UNIQUE (utilisateur_id, chantier_id, permission_id, type),
    CONSTRAINT ck_utilisateur_permission_chantier_type CHECK (type IN ('ACCORDER', 'REFUSER')),
    CONSTRAINT fk_upc_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_upc_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id),
    CONSTRAINT fk_upc_permission FOREIGN KEY (permission_id)
        REFERENCES permission (id),
    CONSTRAINT fk_upc_created_by FOREIGN KEY (created_by)
        REFERENCES utilisateur (id)
);
CREATE INDEX ix_upc_chantier_id ON utilisateur_permission_chantier (chantier_id);
CREATE INDEX ix_upc_utilisateur_id ON utilisateur_permission_chantier (utilisateur_id);

-- ============================================================
-- FIN DE LA MIGRATION V11
-- ============================================================