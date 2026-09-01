package com.cms.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de l'adaptateur Spring Security : l'expression
 * {@code hasPermission('TACHE','MODIFIER')} est traduite vers le controle RBAC
 * du CMS (OBJECTIF 4).
 */
@ExtendWith(MockitoExtension.class)
class SpringPermissionEvaluatorTest {

    @Mock
    private com.cms.security.permission.PermissionEvaluator permissionEvaluator;
    @Mock
    private com.cms.security.service.CurrentUserService currentUserService;
    @Mock
    private com.cms.security.service.DroitsService droitsService;

    private SpringPermissionEvaluator adapter;

    @BeforeEach
    void setUp() {
        adapter = new SpringPermissionEvaluator(permissionEvaluator, currentUserService, droitsService);
    }

    @Test
    void hasPermission_moduleAction_delegueVersLeControleRbac() {
        when(permissionEvaluator.hasPermission("TACHE", "MODIFIER")).thenReturn(true);

        Authentication authentication = new org.springframework.security.authentication.TestingAuthenticationToken(
                "utilisateur", null);

        boolean autorise = adapter.hasPermission(authentication, "TACHE", "MODIFIER");

        assertThat(autorise).isTrue();
        verify(permissionEvaluator).hasPermission("TACHE", "MODIFIER");
    }

    @Test
    void hasPermission_parIdEtType_nonSupportee() {
        Authentication authentication = new org.springframework.security.authentication.TestingAuthenticationToken(
                "utilisateur", null);

        boolean autorise = adapter.hasPermission(authentication, 1L, "Tache", "LIRE");

        assertThat(autorise).isFalse();
    }

}
