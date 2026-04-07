package org.lunskra.adapter.api.mapper;

import org.lunskra.core.validation.FieldError;
import org.lunskra.family_tree.api.model.FieldErrorDto;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper that converts {@link org.lunskra.core.validation.FieldError} domain
 * objects to their API-layer {@code FieldErrorDto} equivalents, used in problem-details
 * error responses.
 */
@Mapper(componentModel = "jakarta")
public interface FieldErrorDtoMapper {

    FieldErrorDto toDto(FieldError entity);
    List<FieldErrorDto> toDto(List<FieldError> entities);
}
