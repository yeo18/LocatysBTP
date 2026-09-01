package com.cms.chantier.service;

import java.util.List;

import com.cms.chantier.dto.AffectationUtilisateurChantierResponse;
import com.cms.chantier.dto.CreateAffectationUtilisateurChantierRequest;
import com.cms.chantier.dto.ModifierAffectationUtilisateurChantierRequest;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;

/**
 * Service des affectations directes utilisateur <-> chantier.
 *
 * <p>Relation ternaire {@code Utilisateur x Chantier x Profil} avec periode :
 * un utilisateur a un seul et unique profil par chantier et par periode.
 */
public interface AffectationUtilisateurChantierService {

    AffectationUtilisateurChantierResponse affecter(Long chantierId, CreateAffectationUtilisateurChantierRequest request);

    AffectationUtilisateurChantierResponse modifier(Long affectationId, ModifierAffectationUtilisateurChantierRequest request);

    void retirer(Long chantierId, Long utilisateurId);

    void supprimer(Long affectationId);

    List<UtilisateurResumeResponse> listerUtilisateursDuChantier(Long chantierId);

    List<AffectationUtilisateurChantierResponse> listerAffectationsDuChantier(Long chantierId);
}