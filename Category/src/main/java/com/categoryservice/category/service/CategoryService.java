package com.categoryservice.category.service;

import com.categoryservice.category.dto.CategoryRequestDTO;
import com.categoryservice.category.dto.CategoryResponseDTO;
import com.categoryservice.category.entity.Category;
import com.categoryservice.category.mapper.CategoryMapper;
import com.categoryservice.category.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        log.info("Category Service: Request to createCategory a new category : {}", categoryRequestDTO);
        Category category = categoryMapper.categoryRequestDTOToCategory(categoryRequestDTO);
        Category savedCategory = categoryRepository.saveAndFlush(category);
        return  categoryMapper.categoryToCategoryResponseDTO(savedCategory);
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        log.info("Category Service: Request to getCategoryById a category : {}", id);
        Category category = categoryRepository.getReferenceById(id);
        return  categoryMapper.categoryToCategoryResponseDTO(category);
    }

    public void deleteCategoryById(Long id){
        log.info("Category Service: Request to delete a category : {}", id);
        Category category = categoryRepository.getReferenceById(id);
        categoryRepository.delete(category);
    }

    public Page<CategoryResponseDTO> getAllCategories(int page, int size) {
        log.info("Category Service: Getting All Categories - page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageRequest);
        return categoryPage.map(categoryMapper::categoryToCategoryResponseDTO);
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        log.info("Category Service: Updating category with id: {} - {}", id, categoryRequestDTO);
        Category existingCategory = categoryRepository.getReferenceById(id);

        existingCategory.setCategoryId(categoryRequestDTO.categoryId());
        existingCategory.setCategoryName(categoryRequestDTO.categoryName());

        Category updatedCategory = categoryRepository.saveAndFlush(existingCategory);
        log.info("Category Service: Category updated successfully: {}", updatedCategory);
        return categoryMapper.categoryToCategoryResponseDTO(updatedCategory);
    }
}
