package com.cms.template.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Association entre un {@link TemplateChantier} et un {@link TemplateTache}.
 *
 * <p>Table : {@code template_chantier_tache} (MLD). Un même
 * {@link TemplateTache} peut être associé à plusieurs templates (réutilisation).
 */
@Entity
@Table(name = "template_chantier_tache", uniqueConstraints = {
        @UniqueConstraint(name = "uk_template_chantier_tache_tache",
                columnNames = {"template_chantier_id", "template_tache_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateChantierTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_chantier_id", nullable = false)
    private TemplateChantier templateChantier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_tache_id", nullable = false)
    private TemplateTache templateTache;
}
