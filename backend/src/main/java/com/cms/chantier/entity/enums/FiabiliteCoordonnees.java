package com.cms.chantier.entity.enums;

/**
 * Fiabilite des coordonnees d'un chantier.
 *
 * <p>{@code APPROXIMATIVE} : position proposee par le geocodage ;
 * {@code PRECISE} : position confirmee / corrigee par l'utilisateur.
 */
public enum FiabiliteCoordonnees {
    APPROXIMATIVE,
    PRECISE
}
