-- ============================================================
-- MIGRATION V3 : Permissions RBAC module Template
-- ============================================================
-- LOOP 5.11 — Ajout des permissions du module Template au
-- referentiel RBAC (mecanisme dynamique existant).
--
-- Convention : code MODULE_ACTION (MAJUSCULES), identique a V1.
--
-- Permissions creees (uniquement celles reellement necessaires) :
--   * TEMPLATE_CHANTIER_LIRE / CREER / MODIFIER
--        - la desactivation d'un TemplateChantier est protegee par
--          TEMPLATE_CHANTIER_MODIFIER (convention PermissionController :
--          aucune action DESACTIVER separee).
--        - pas de SUPPRIMER : le modele prevoit une desactivation
--          logique, aucun DELETE physique.
--   * TEMPLATE_TACHE_LIRE / CREER / MODIFIER
--        - pas de SUPPRIMER : aucun endpoint de suppression.
--        - pas de DESACTIVER : TemplateTache ne possede PAS de statut.
--
-- IMPORT : aucune permission IMPORT dediee. Un import est protege par
-- la permission LIRE du template concerne (utiliser le template) + le
-- controle d'acces au chantier cible (perimetre, Niveau 2, dans le
-- Service).
--
-- Le profil ADMINISTRATEUR obtient automatiquement ces permissions via
-- DroitsService (relecture du referentiel en base) : aucune ligne
-- profil_permission a inserer ici. UTILISATEUR_STANDARD n'obtient
-- aucune permission Template par defaut (moindre privilege).
-- ============================================================

INSERT INTO permission (nom, code_permission, module, action, description) VALUES
    ('Lire un template de chantier',       'TEMPLATE_CHANTIER_LIRE',       'TEMPLATE_CHANTIER', 'LIRE',       'Consultation des templates de chantier'),
    ('Créer un template de chantier',      'TEMPLATE_CHANTIER_CREER',      'TEMPLATE_CHANTIER', 'CREER',      'Création d''un template de chantier'),
    ('Modifier un template de chantier',   'TEMPLATE_CHANTIER_MODIFIER',   'TEMPLATE_CHANTIER', 'MODIFIER',   'Modification et désactivation d''un template de chantier'),
    ('Lire un template de tâche',          'TEMPLATE_TACHE_LIRE',          'TEMPLATE_TACHE',    'LIRE',       'Consultation des templates de tâches'),
    ('Créer un template de tâche',         'TEMPLATE_TACHE_CREER',         'TEMPLATE_TACHE',    'CREER',      'Création d''un template de tâches'),
    ('Modifier un template de tâche',      'TEMPLATE_TACHE_MODIFIER',      'TEMPLATE_TACHE',    'MODIFIER',   'Modification d''un template de tâches');

-- ============================================================
-- FIN DE LA MIGRATION V3
-- ============================================================
