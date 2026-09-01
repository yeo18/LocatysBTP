package com.cms.repository;

import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.profil.repository.ProfilRepository;
import com.cms.tache.repository.AffectationTacheRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.tache.repository.ValidationTacheRepository;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de démarrage : vérifie l'injection des Repository dans le contexte
 * Spring et l'absence d'erreur JPA (métamodèle Hibernate chargé).
 */
@SpringBootTest
class RepositoryInjectionTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private UtilisateurPermissionRepository utilisateurPermissionRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private ProfilPermissionRepository profilPermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private ChantierRepository chantierRepository;
    @Autowired
    private AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private MembreEquipeRepository membreEquipeRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private AffectationTacheRepository affectationTacheRepository;
    @Autowired
    private ValidationTacheRepository validationTacheRepository;

    @Test
    void tousLesRepositoriesSontInjectes() {
        assertThat(utilisateurRepository).isNotNull();
        assertThat(utilisateurPermissionRepository).isNotNull();
        assertThat(profilRepository).isNotNull();
        assertThat(profilPermissionRepository).isNotNull();
        assertThat(permissionRepository).isNotNull();
        assertThat(chantierRepository).isNotNull();
        assertThat(affectationEquipeChantierRepository).isNotNull();
        assertThat(equipeRepository).isNotNull();
        assertThat(membreEquipeRepository).isNotNull();
        assertThat(tacheRepository).isNotNull();
        assertThat(affectationTacheRepository).isNotNull();
        assertThat(validationTacheRepository).isNotNull();
    }
}
