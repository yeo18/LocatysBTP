-- ============================================================
-- MIGRATION V9 : Affectation directe utilisateur <-> chantier
-- ============================================================
-- L'administrateur assigne directement des utilisateurs à un
-- chantier (indépendamment des équipes). Ces utilisateurs deviennent
-- les candidats des équipes de ce chantier, et voient le chantier
-- (sécurité par données).
-- ============================================================

CREATE TABLE affectation_utilisateur_chantier (
    id                  BIGSERIAL       PRIMARY KEY,
    utilisateur_id      BIGINT          NOT NULL,
    chantier_id         BIGINT          NOT NULL,
    date_affectation    TIMESTAMP       NOT NULL,
    CONSTRAINT uk_affectation_utilisateur_chantier UNIQUE (utilisateur_id, chantier_id),
    CONSTRAINT fk_affectation_utilisateur_chantier_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id),
    CONSTRAINT fk_affectation_utilisateur_chantier_chantier FOREIGN KEY (chantier_id)
        REFERENCES chantier (id)
);
CREATE INDEX ix_affectation_utilisateur_chantier_chantier_id ON affectation_utilisateur_chantier (chantier_id);
CREATE INDEX ix_affectation_utilisateur_chantier_utilisateur_id ON affectation_utilisateur_chantier (utilisateur_id);

-- ============================================================
-- FIN DE LA MIGRATION V9
-- ============================================================
