package com.cms.template.mapper;

import org.junit.jupiter.api.Test;

import com.cms.tache.entity.enums.Priorite;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.entity.TemplateTache;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper TemplateTache (aucun acces Spring/DB).
 */
class TemplateTacheMapperTest {

    private final TemplateTacheMapper mapper = new TemplateTacheMapperImpl();

    @Test
    void requestVersEntity_mappeChampsSaisis() {
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre("Dalle beton");
        request.setDescription("Coulage dalle");
        request.setPriorite(Priorite.HAUTE);
        request.setDureeEstimeeJours(5);

        TemplateTache entity = mapper.toEntity(request);

        assertThat(entity.getTitre()).isEqualTo("Dalle beton");
        assertThat(entity.getDescription()).isEqualTo("Coulage dalle");
        assertThat(entity.getPriorite()).isEqualTo(Priorite.HAUTE);
        assertThat(entity.getDureeEstimeeJours()).isEqualTo(5);
    }

    @Test
    void requestVersEntity_champsServuerIgnores() {
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre("Toiture tole");

        TemplateTache entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDateCreation()).isNull();
        assertThat(entity.getDateModification()).isNull();
        assertThat(entity.getCreatedBy()).isNull();
        assertThat(entity.getTemplates()).isEmpty();
    }

    @Test
    void entityVersResponse_mappeSansRelations() {
        TemplateTache entity = new TemplateTache();
        entity.setId(1L);
        entity.setTitre("Dalle beton");
        entity.setDescription("Coulage dalle");
        entity.setPriorite(Priorite.MOYENNE);
        entity.setDureeEstimeeJours(5);

        TemplateTacheResponse response = mapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitre()).isEqualTo("Dalle beton");
        assertThat(response.getPriorite()).isEqualTo(Priorite.MOYENNE);
        assertThat(response.getDureeEstimeeJours()).isEqualTo(5);
    }

    @Test
    void entityVersResponse_nExposePasLEntity() {
        TemplateTache entity = new TemplateTache();
        entity.setId(1L);
        entity.setTitre("Dalle beton");

        TemplateTacheResponse response = mapper.toResponse(entity);

        assertThat(response).isNotInstanceOf(TemplateTache.class);
    }

    @Test
    void entityVersResume_mappeChampsMinimaux() {
        TemplateTache entity = new TemplateTache();
        entity.setId(1L);
        entity.setTitre("Dalle beton");
        entity.setPriorite(Priorite.HAUTE);

        TemplateTacheResumeResponse resume = mapper.toResumeResponse(entity);

        assertThat(resume.getId()).isEqualTo(1L);
        assertThat(resume.getTitre()).isEqualTo("Dalle beton");
        assertThat(resume.getPriorite()).isEqualTo(Priorite.HAUTE);
    }

    @Test
    void update_mappeChampsModifiablesSansToucherIdNiDates() {
        TemplateTache entity = new TemplateTache();
        entity.setId(1L);
        entity.setTitre("Dalle beton");
        entity.setPriorite(Priorite.HAUTE);

        UpdateTemplateTacheRequest request = new UpdateTemplateTacheRequest();
        request.setTitre("Dalle beton armee");
        request.setPriorite(Priorite.MOYENNE);

        mapper.update(entity, request);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTitre()).isEqualTo("Dalle beton armee");
        assertThat(entity.getPriorite()).isEqualTo(Priorite.MOYENNE);
    }

    @Test
    void absenceDeStatutSurTemplateTache() {
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre("Dalle beton");

        TemplateTache entity = mapper.toEntity(request);

        // Le modele TemplateTache ne possede pas de statut ACTIF/INACTIF :
        // aucun attribut ne doit etre expose ni mappe.
        assertThat(TemplateTache.class.getDeclaredFields())
                .noneMatch(field -> field.getName().equals("statut"));
    }
}
