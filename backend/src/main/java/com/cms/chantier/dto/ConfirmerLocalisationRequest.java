package com.cms.chantier.dto;

import java.math.BigDecimal;

import com.cms.chantier.entity.enums.FiabiliteCoordonnees;
import com.cms.chantier.entity.enums.OrigineCoordonnees;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Confirmation de la position gÃ©ographique d'un chantier.
 *
 * <p>La position provient soit du gÃ©ocodage (source GEOCODAGE, prÃ©cision
 * APPROXIMATIVE) soit d'une correction manuelle sur la carte (source CARTE,
 * prÃ©cision PRECISE). La confirmation est obligatoire avant toute analyse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmerLocalisationRequest {

    @NotNull
    @DecimalMin(value = "-90.0", message = "Latitude hors bornes")
    @DecimalMax(value = "90.0", message = "Latitude hors bornes")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitude hors bornes")
    @DecimalMax(value = "180.0", message = "Longitude hors bornes")
    private BigDecimal longitude;

    @Size(max = 300)
    private String adresseGeocodee;

    @NotNull
    private OrigineCoordonnees origineCoordonnees;

    @NotNull
    private FiabiliteCoordonnees fiabiliteCoordonnees;
}
