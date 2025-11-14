package com.categoryservice.category.mapper;

import com.categoryservice.category.dto.CategoryRequestDTO;
import com.categoryservice.category.dto.CategoryResponseDTO;
import com.categoryservice.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO categoryToCategoryResponseDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getCategoryId(),
                category.getCategoryName()
        );
    }

    public Category categoryRequestDTOToCategory(CategoryRequestDTO categoryRequestDTO) {
        return Category.builder()
                .categoryId(categoryRequestDTO.categoryId())
                .categoryName(categoryRequestDTO.categoryName())
                .build();
    }
}
