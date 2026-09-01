package com.cms.mapper;

import com.cms.chantier.mapper.ChantierMapper;
import com.cms.equipe.mapper.AffectationEquipeChantierMapper;
import com.cms.equipe.mapper.EquipeMapper;
import com.cms.equipe.mapper.MembreEquipeMapper;
import com.cms.permission.mapper.PermissionMapper;
import com.cms.profil.mapper.ProfilMapper;
import com.cms.profil.mapper.ProfilPermissionMapper;
import com.cms.tache.mapper.AffectationTacheMapper;
import com.cms.tache.mapper.TacheMapper;
import com.cms.tache.mapper.ValidationTacheMapper;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.mapper.UtilisateurPermissionMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de démarrage : vérifie la génération et l'injection des Mapper
 * MapStruct dans le contexte Spring (componentModel = spring).
 */
@SpringBootTest
class MapperInjectionTest {

    @Autowired
    private UtilisateurMapper utilisateurMapper;
    @Autowired
    private UtilisateurPermissionMapper utilisateurPermissionMapper;
    @Autowired
    private ProfilMapper profilMapper;
    @Autowired
    private ProfilPermissionMapper profilPermissionMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private ChantierMapper chantierMapper;
    @Autowired
    private AffectationEquipeChantierMapper affectationEquipeChantierMapper;
    @Autowired
    private EquipeMapper equipeMapper;
    @Autowired
    private MembreEquipeMapper membreEquipeMapper;
    @Autowired
    private TacheMapper tacheMapper;
    @Autowired
    private AffectationTacheMapper affectationTacheMapper;
    @Autowired
    private ValidationTacheMapper validationTacheMapper;

    @Test
    void tousLesMappersSontInjectes() {
        assertThat(utilisateurMapper).isNotNull();
        assertThat(utilisateurPermissionMapper).isNotNull();
        assertThat(profilMapper).isNotNull();
        assertThat(profilPermissionMapper).isNotNull();
        assertThat(permissionMapper).isNotNull();
        assertThat(chantierMapper).isNotNull();
        assertThat(affectationEquipeChantierMapper).isNotNull();
        assertThat(equipeMapper).isNotNull();
        assertThat(membreEquipeMapper).isNotNull();
        assertThat(tacheMapper).isNotNull();
        assertThat(affectationTacheMapper).isNotNull();
        assertThat(validationTacheMapper).isNotNull();
    }
}
