package com.cms.chantier.entity.enums;

/**
 * Origine des coordonnees d'un chantier.
 *
 * <p>{@code GEOCODAGE} : position issue d'une recherche Nominatim ;
 * {@code CARTE} : position corrigee manuellement par l'utilisateur.
 */
public enum OrigineCoordonnees {
    GEOCODAGE,
    CARTE
}
