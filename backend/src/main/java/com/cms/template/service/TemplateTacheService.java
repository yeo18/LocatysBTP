package com.cms.template.service;

import java.util.List;

import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.CreateTemplateTacheTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.dto.UpdateTemplateTacheTacheRequest;

/**
 * Service metier du module Template — TemplateTache (LOOP 5.8).
 *
 * <p>Un TemplateTache est un groupe reutilisable de taches (ex. Dalle
 * beton, Toiture tole). Il peut etre utilise dans plusieurs
 * TemplateChantier et importe directement dans un chantier existant.
 * L'import est une COPIE (snapshot) : les taches creees restent
 * independantes du template.
 *
 * <p>Note : conformement au modele valide, un TemplateTache n'a pas
 * d'attribut statut (pas de desactivation) — seule la desactivation d'un
 * TemplateChantier existe.
 */
public interface TemplateTacheService {

    /**
     * Cree un TemplateTache.
     *
     * @param request donnees valides
     * @return template cree
     */
    TemplateTacheResponse creer(CreateTemplateTacheRequest request);

    /**
     * Modifie un TemplateTache.
     *
     * @param id      identifiant du template
     * @param request donnees de modification
     * @return template modifie
     */
    TemplateTacheResponse modifier(Long id, UpdateTemplateTacheRequest request);

    /**
     * Consulte un TemplateTache par identifiant.
     *
     * @param id identifiant du template
     * @return template
     */
    TemplateTacheResponse trouverParId(Long id);

    /**
     * Liste tous les TemplateTache.
     *
     * @return templates
     */
    List<TemplateTacheResponse> lister();

    /**
     * Liste les TemplateTache dont le titre contient la chaine donnee
     * (recherche insensible a la casse, deleguee cote base).
     *
     * @param nom chaine de recherche (titre partiel)
     * @return templates correspondants
     */
    List<TemplateTacheResponse> listerParNom(String nom);

    /**
     * Importe un TemplateTache dans un chantier existant : copie (snapshot)
     * le template en une nouvelle Tache du chantier.
     *
     * @param templateTacheId identifiant du template de tache
     * @param chantierId      identifiant du chantier cible
     * @return nombre de taches creees
     */
    int importerDansChantier(Long templateTacheId, Long chantierId);

    // ------------------------------------------------------------------
    // Taches structurees d'un TemplateTache (LOOP §1, §2, §18)
    // ------------------------------------------------------------------

    /**
     * Ajoute une tache structuree a un TemplateTache.
     *
     * @param templateTacheId identifiant de l'en-tete
     * @param request         donnees de la tache structuree
     * @return tache structuree creee
     */
    TemplateTacheTacheResponse ajouterTache(Long templateTacheId, CreateTemplateTacheTacheRequest request);

    /**
     * Importe une tache reelle existante comme tache structuree (copie des
     * champs : titre, description, priorite, duree). Snapshot : aucune
     * dependance avec la tache source.
     *
     * @param templateTacheId identifiant de l'en-tete
     * @param tacheId         identifiant de la tache reelle a copier
     * @return tache structuree creee
     */
    TemplateTacheTacheResponse importerTache(Long templateTacheId, Long tacheId);

    /**
     * Modifie une tache structuree (titre, description, priorite, duree).
     *
     * @param templateTacheId identifiant de l'en-tete
     * @param itemId          identifiant de la tache structuree
     * @param request         donnees de modification
     * @return tache structuree modifiee
     */
    TemplateTacheTacheResponse modifierTache(Long templateTacheId, Long itemId, UpdateTemplateTacheTacheRequest request);

    /**
     * Retire une tache structuree d'un TemplateTache.
     *
     * @param templateTacheId identifiant de l'en-tete
     * @param itemId          identifiant de la tache structuree
     */
    void retirerTache(Long templateTacheId, Long itemId);

    /**
     * Liste les taches structurees d'un TemplateTache, ordonnees.
     *
     * @param templateTacheId identifiant de l'en-tete
     * @return taches structurees
     */
    List<TemplateTacheTacheResumeResponse> listerTaches(Long templateTacheId);
}
