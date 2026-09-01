package com.cms.template.service;

import java.util.List;

import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.ImportTemplateChantierAffectationRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;

/**
 * Service metier du module Template — TemplateChantier (LOOP 5.8).
 *
 * <p>Un TemplateChantier est un modele complet de construction (ex. Maison
 * R+1, Immeuble R+5). Il regroupe des TemplateTache (association ordonnee)
 * et peut etre importe dans un chantier : l'import est une COPIE (snapshot)
 * — les donnees creees restent independantes du template.
 */
public interface TemplateChantierService {

    /**
     * Cree un TemplateChantier (statut ACTIF par defaut).
     *
     * @param request donnees valides
     * @return template cree
     */
    TemplateChantierResponse creer(CreateTemplateChantierRequest request);

    /**
     * Modifie un TemplateChantier.
     *
     * @param id      identifiant du template
     * @param request donnees de modification
     * @return template modifie
     */
    TemplateChantierResponse modifier(Long id, UpdateTemplateChantierRequest request);

    /**
     * Desactive un TemplateChantier (statut INACTIF).
     *
     * @param id identifiant du template
     * @return template desactive
     */
    TemplateChantierResponse desactiver(Long id);

    /**
     * Consulte un TemplateChantier par identifiant.
     *
     * @param id identifiant du template
     * @return template
     */
    TemplateChantierResponse trouverParId(Long id);

    /**
     * Liste tous les TemplateChantier (actifs et inactifs).
     *
     * @return templates
     */
    List<TemplateChantierResponse> lister();

    /**
     * Liste les TemplateChantier dont le nom contient la chaine donnee
     * (recherche insensible a la casse, deleguee cote base).
     *
     * @param nom chaine de recherche (nom partiel)
     * @return templates correspondants
     */
    List<TemplateChantierResponse> listerParNom(String nom);

    /**
     * Associe un TemplateTache a un TemplateChantier (CAS 3).
     *
     * @param templateChantierId identifiant du template de chantier
     * @param templateTacheId    identifiant du template de tache
     */
    void associerTemplateTache(Long templateChantierId, Long templateTacheId);

    /**
     * Retire l'association d'un TemplateTache a un TemplateChantier.
     *
     * @param templateChantierId identifiant du template de chantier
     * @param templateTacheId    identifiant du template de tache
     */
    void retirerTemplateTache(Long templateChantierId, Long templateTacheId);

    /**
     * Consulte les TemplateTache associes a un TemplateChantier.
     *
     * @param templateChantierId identifiant du template de chantier
     * @return templates de taches associes
     */
    List<TemplateTacheResumeResponse> trouverTachesAssociees(Long templateChantierId);

    /**
     * Importe un TemplateChantier dans un chantier existant.
     *
     * <p>Copie (snapshot) chaque TemplateTache associe en une nouvelle
     * Tache du chantier (duree estimee NON copiee). Les
     * donnees creees restent independantes du template.
     *
     * @param templateChantierId identifiant du template
     * @param chantierId         identifiant du chantier cible
     * @return nombre de taches creees
     */
    int importerDansChantier(Long templateChantierId, Long chantierId);

    /**
     * Importe un TemplateChantier dans un chantier existant, en attribuant
     * les taches creees a des utilisateurs (affectations optionnelles).
     *
     * <p>Chaque affectation de la liste reference un {@code templateTacheId}
     * (identifiant d'une TemplateTache associee au template) : lorsque la
     * tache copiee correspond, une {@code AffectationTache} est creee pour
     * l'utilisateur et le role demandes (REALISATEUR/CONTROLEUR).
     *
     * @param templateChantierId identifiant du template
     * @param chantierId         identifiant du chantier cible
     * @param affectations       affectations a creer (peut etre vide/null)
     * @return nombre de taches creees
     */
    int importerDansChantier(Long templateChantierId, Long chantierId,
                             List<ImportTemplateChantierAffectationRequest> affectations);
}
