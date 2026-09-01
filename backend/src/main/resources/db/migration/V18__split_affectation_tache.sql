-- ------------------------------------------------------------
-- V18 : scission de affectation_tache en 2 tables distinctes.
-- Une tâche est désormais affectée à un utilisateur (affectation_tache_utilisateur)
-- OU à une équipe (affectation_tache_equipe), en relations binaires
-- indépendantes. Les données existantes (chacune ne ciblant qu'une seule
-- cible) sont réparties dans la bonne table, puis l'ancienne table est
-- supprimée.
-- ------------------------------------------------------------

CREATE TABLE affectation_tache_utilisateur (
    id                  BIGSERIAL       PRIMARY KEY,
    tache_id            BIGINT          NOT NULL,
    utilisateur_id      BIGINT          NOT NULL,
    role                VARCHAR(15)     NOT NULL,
    date_affectation    DATE            NOT NULL,
    CONSTRAINT ck_atu_role CHECK (role IN ('REALISATEUR', 'CONTROLEUR')),
    CONSTRAINT fk_atu_tache FOREIGN KEY (tache_id) REFERENCES tache (id),
    CONSTRAINT fk_atu_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
);
CREATE INDEX ix_atu_tache_id ON affectation_tache_utilisateur (tache_id);
CREATE INDEX ix_atu_utilisateur_id ON affectation_tache_utilisateur (utilisateur_id);

CREATE TABLE affectation_tache_equipe (
    id                  BIGSERIAL       PRIMARY KEY,
    tache_id            BIGINT          NOT NULL,
    equipe_id           BIGINT          NOT NULL,
    role                VARCHAR(15)     NOT NULL,
    date_affectation    DATE            NOT NULL,
    CONSTRAINT ck_ate_role CHECK (role IN ('REALISATEUR', 'CONTROLEUR')),
    CONSTRAINT fk_ate_tache FOREIGN KEY (tache_id) REFERENCES tache (id),
    CONSTRAINT fk_ate_equipe FOREIGN KEY (equipe_id) REFERENCES equipe (id)
);
CREATE INDEX ix_ate_tache_id ON affectation_tache_equipe (tache_id);
CREATE INDEX ix_ate_equipe_id ON affectation_tache_equipe (equipe_id);

INSERT INTO affectation_tache_utilisateur (tache_id, utilisateur_id, role, date_affectation)
SELECT tache_id, utilisateur_id, role, date_affectation
FROM affectation_tache
WHERE utilisateur_id IS NOT NULL;

INSERT INTO affectation_tache_equipe (tache_id, equipe_id, role, date_affectation)
SELECT tache_id, equipe_id, role, date_affectation
FROM affectation_tache
WHERE equipe_id IS NOT NULL;

DROP TABLE affectation_tache;
