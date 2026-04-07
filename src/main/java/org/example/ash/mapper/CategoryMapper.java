package org.example.ash.mapper;

import org.example.ash.dto.CategoryDTO;
import org.example.ash.dto.request.CategoryRequest;
import org.example.ash.entity.oracle.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category, CategoryRequest> {

}
