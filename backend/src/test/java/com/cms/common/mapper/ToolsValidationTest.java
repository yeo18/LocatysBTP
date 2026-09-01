package com.cms.common.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test technique de validation du LOOP 1.6.
 * Verifie que Lombok, MapStruct (Spring Component Model) et
 * Bean Validation fonctionnent ensemble.
 */
@SpringBootTest
@ActiveProfiles("test")
class ToolsValidationTest {

    @Autowired
    private ToolsValidationMapper mapper;

    @Autowired
    private Validator validator;

    @Test
    void mapstructCopieLesChamps() {
        ToolsValidationSource source = new ToolsValidationSource();
        source.setNom("Chantier Alpha");
        source.setEmail("contact@cms.example.com");
        source.setQuantite(10);

        ToolsValidationTarget target = new ToolsValidationTarget();
        mapper.copy(source, target);

        assertThat(target.getNom()).isEqualTo("Chantier Alpha");
        assertThat(target.getEmail()).isEqualTo("contact@cms.example.com");
        assertThat(target.getQuantite()).isEqualTo(10);
    }

    @Test
    void beanValidationDetecteLesViolations() {
        ToolsValidationSource source = new ToolsValidationSource();
        source.setNom("");       // @NotBlank -> violation
        source.setEmail("pas-un-email"); // @Email -> violation
        source.setQuantite(-5);  // @Positive -> violation

        Set<ConstraintViolation<ToolsValidationSource>> violations = validator.validate(source);
        assertThat(violations).hasSize(3);
    }

    @Test
    void beanValidationAccepteObjetValide() {
        ToolsValidationSource source = new ToolsValidationSource();
        source.setNom("Valide");
        source.setEmail("ok@cms.example.com");
        source.setQuantite(3);

        assertThat(validator.validate(source)).isEmpty();
    }

}
