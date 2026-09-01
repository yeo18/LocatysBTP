-- ============================================================
-- MIGRATION V12 : Analyse du site — localisation du chantier
-- ============================================================
-- Ajoute la position géographique d'un chantier (issue du géocodage
-- ou corrigée sur la carte) et les métadonnées associées.
--
--   latitude              : coordonnée proposée par Nominatim (géocodage)
--   longitude             : idem
--   localisation_description : description libre saisie par l'utilisateur
--   source_localisation   : GEOCODAGE | CARTE
--   precision_localisation: APPROXIMATIVE | PRECISE
--
-- Rien n'est supprimé ni renommé : ajout strictement additif.
-- ============================================================

ALTER TABLE chantier
    ADD COLUMN latitude NUMERIC(10, 7),
    ADD COLUMN longitude NUMERIC(10, 7),
    ADD COLUMN localisation_description VARCHAR(300),
    ADD COLUMN source_localisation VARCHAR(20),
    ADD COLUMN precision_localisation VARCHAR(20);

ALTER TABLE chantier
    ADD CONSTRAINT ck_chantier_source_localisation
        CHECK (source_localisation IS NULL OR source_localisation IN ('GEOCODAGE', 'CARTE'));

ALTER TABLE chantier
    ADD CONSTRAINT ck_chantier_precision_localisation
        CHECK (precision_localisation IS NULL OR precision_localisation IN ('APPROXIMATIVE', 'PRECISE'));

-- ============================================================
-- FIN DE LA MIGRATION V12
-- ============================================================
