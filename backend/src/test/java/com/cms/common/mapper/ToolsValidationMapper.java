package com.cms.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * Interface technique de demonstration pour valider la chaine
 * Lombok + MapStruct (Spring Component Model) + Bean Validation.
 *
 * A SUPPRIMER : sert uniquement au LOOP 1.6 pour prouver que les
 * outils compilent et fonctionnent ensemble. Aucune logique metier.
 */
@Mapper(componentModel = "spring")
public interface ToolsValidationMapper {

    void copy(ToolsValidationSource source, @MappingTarget ToolsValidationTarget target);

}
