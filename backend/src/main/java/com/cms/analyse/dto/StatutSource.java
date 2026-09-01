package com.cms.analyse.dto;

/**
 * Statut d'une source d'analyse indépendante.
 *
 * <p>Chaque source (meteo, environnement) est
 * indépendante : une source indisponible ne fait jamais échouer l'analyse.
 */
public enum StatutSource {
    /** La source a répondu et renvoyé des données exploitables. */
    DISPONIBLE,
    /** La source n'a pas renvoyé de résultat (aucune donnée trouvée). */
    INDISPONIBLE,
    /** La source a renvoyé une erreur (API inaccessible, quota, timeout...). */
    ERREUR
}
