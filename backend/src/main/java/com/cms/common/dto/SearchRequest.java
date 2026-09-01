package com.cms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objet de recherche et de filtrage pagine.
 *
 * <p>Utilise par tous les endpoints de liste. Les recherches avancees
 * seront implementees dans les Services (Specifications).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    private int page = 0;
    private int size = 20;
    private String sort;
    private String direction = "ASC";
    private String motCle;
}
