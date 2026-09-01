package com.cms.analyse.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Cache TTL simple en mémoire (thread-safe), utilisé pour les réponses des
 * APIs externes. Pas de Redis : le projet n'en a pas besoin pour cette
 * fonctionnalité.
 *
 * @param <K> type de la cle
 * @param <V> type de la valeur
 */
@Component
public class TtlCache {

    private record Entree<V>(V valeur, Instant expiration) {
    }

    private final Map<String, Entree<Object>> store = new ConcurrentHashMap<>();

    /**
     * Récupère une valeur si elle est encore valide.
     *
     * @param cle      cle de la valeur
     * @param <V>      type de la valeur
     * @return la valeur ou {@code null} si absente/expirée
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String cle) {
        Entree<Object> entree = store.get(cle);
        if (entree == null) {
            return null;
        }
        if (entree.expiration().isBefore(Instant.now())) {
            store.remove(cle);
            return null;
        }
        return (V) entree.valeur();
    }

    /**
     * Met en cache une valeur pour une durée donnée.
     *
     * @param cle        cle de la valeur
     * @param valeur     valeur à cacher
     * @param dureeSecon durée de vie en secondes
     */
    public void put(String cle, Object valeur, long dureeSecon) {
        store.put(cle, new Entree<>(valeur, Instant.now().plusSeconds(dureeSecon)));
    }

}
