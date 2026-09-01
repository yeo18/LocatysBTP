package com.cms.template.mapper;


import org.junit.jupiter.api.Test;

import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateChantierResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.enums.TypeConstruction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper TemplateChantier (aucun acces Spring/DB).
 */
class TemplateChantierMapperTest {

    private final TemplateChantierMapper mapper = new TemplateChantierMapperImpl();

    @Test
    void requestVersEntity_mappeChampsSaisis() {
        CreateTemplateChantierRequest request = new CreateTemplateChantierRequest();
        request.setNom("Maison R+1");
        request.setDescription("Construction complete");
        request.setTypeConstruction(TypeConstruction.MAISON_R1);
        request.setDureeEstimeeJours(90);

        TemplateChantier entity = mapper.toEntity(request);

        assertThat(entity.getNom()).isEqualTo("Maison R+1");
        assertThat(entity.getDescription()).isEqualTo("Construction complete");
        assertThat(entity.getTypeConstruction()).isEqualTo(TypeConstruction.MAISON_R1);
        assertThat(entity.getDureeEstimeeJours()).isEqualTo(90);
    }

    @Test
    void requestVersEntity_champsServuerIgnores() {
        CreateTemplateChantierRequest request = new CreateTemplateChantierRequest();
        request.setNom("Villa");

        TemplateChantier entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        // statut non mappe depuis le client : valeur par defaut de l'entite (ACTIF)
        assertThat(entity.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
        assertThat(entity.getDateCreation()).isNull();
        assertThat(entity.getDateModification()).isNull();
        assertThat(entity.getCreatedBy()).isNull();
        assertThat(entity.getTaches()).isEmpty();
    }

    @Test
    void entityVersResponse_mappeSansRelations() {
        TemplateChantier entity = new TemplateChantier();
        entity.setId(1L);
        entity.setNom("Immeuble R+5");
        entity.setDescription("Bureau");
        entity.setTypeConstruction(TypeConstruction.IMMEUBLE_R5);
        entity.setDureeEstimeeJours(180);
        entity.setStatut(TemplateChantierStatut.ACTIF);

        TemplateChantierResponse response = mapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNom()).isEqualTo("Immeuble R+5");
        assertThat(response.getTypeConstruction()).isEqualTo(TypeConstruction.IMMEUBLE_R5);
        assertThat(response.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
    }

    @Test
    void entityVersResponse_nExposePasLEntity() {
        TemplateChantier entity = new TemplateChantier();
        entity.setId(1L);
        entity.setNom("Maison R+1");
        entity.setStatut(TemplateChantierStatut.ACTIF);

        TemplateChantierResponse response = mapper.toResponse(entity);

        assertThat(response).isNotInstanceOf(TemplateChantier.class);
    }

    @Test
    void entityVersResume_mappeChampsMinimaux() {
        TemplateChantier entity = new TemplateChantier();
        entity.setId(1L);
        entity.setNom("Maison R+1");
        entity.setTypeConstruction(TypeConstruction.MAISON_R1);
        entity.setStatut(TemplateChantierStatut.ACTIF);

        TemplateChantierResumeResponse resume = mapper.toResumeResponse(entity);

        assertThat(resume.getId()).isEqualTo(1L);
        assertThat(resume.getNom()).isEqualTo("Maison R+1");
        assertThat(resume.getTypeConstruction()).isEqualTo(TypeConstruction.MAISON_R1);
        assertThat(resume.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
    }

    @Test
    void update_mappeChampsModifiablesSansToucherIdNiDates() {
        TemplateChantier entity = new TemplateChantier();
        entity.setId(1L);
        entity.setNom("Maison R+1");
        entity.setTypeConstruction(TypeConstruction.MAISON_R1);
        entity.setStatut(TemplateChantierStatut.INACTIF);

        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Villa");
        request.setTypeConstruction(TypeConstruction.VILLA);
        request.setDureeEstimeeJours(120);

        mapper.update(entity, request);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNom()).isEqualTo("Villa");
        assertThat(entity.getTypeConstruction()).isEqualTo(TypeConstruction.VILLA);
        assertThat(entity.getDureeEstimeeJours()).isEqualTo(120);
    }
}
