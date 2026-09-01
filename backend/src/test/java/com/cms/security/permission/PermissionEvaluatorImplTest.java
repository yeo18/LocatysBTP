package com.cms.security.permission;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.security.service.CurrentUserService;
import com.cms.security.service.DroitsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests du controleur de permissions (OBJECTIF 4) : recuperation de
 * l'utilisateur connecte, calcul des droits effectifs, autorisation/refus.
 */
@ExtendWith(MockitoExtension.class)
class PermissionEvaluatorImplTest {

    private static final long UTILISATEUR_ID = 3L;

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DroitsService droitsService;

    private PermissionEvaluatorImpl evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PermissionEvaluatorImpl(currentUserService, droitsService);
    }

    private void droitesEffectifs(String... codes) {
        when(currentUserService.getCurrentUserId()).thenReturn(UTILISATEUR_ID);
        when(droitsService.calculerDroits(UTILISATEUR_ID)).thenReturn(Set.of(codes));
    }

    @Test
    void hasPermission_moduleAction_autorise() {
        droitesEffectifs("TACHE_LIRE", "CHANTIER_LIRE");

        assertThat(evaluator.hasPermission("TACHE", "LIRE")).isTrue();
    }

    @Test
    void hasPermission_moduleAction_refuse() {
        droitesEffectifs("TACHE_LIRE");

        assertThat(evaluator.hasPermission("TACHE", "MODIFIER")).isFalse();
    }

    @Test
    void hasPermission_moduleAction_insensibleALaCasse() {
        droitesEffectifs("TACHE_VALIDER");

        assertThat(evaluator.hasPermission("tache", "valider")).isTrue();
    }

    @Test
    void hasPermission_codeDirect() {
        droitesEffectifs("UTILISATEUR_SUPPRIMER");

        assertThat(evaluator.hasPermission("UTILISATEUR_SUPPRIMER")).isTrue();
        assertThat(evaluator.hasPermission("UTILISATEUR_CREER")).isFalse();
    }

}
