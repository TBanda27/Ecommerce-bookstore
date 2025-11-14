package com.categoryservice.category.controller;

import com.categoryservice.category.dto.CategoryRequestDTO;
import com.categoryservice.category.dto.CategoryResponseDTO;
import com.categoryservice.category.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        log.info("Category Controller: Request to createCategory a new category : {}", categoryRequestDTO);
        return new ResponseEntity<>(categoryService.createCategory(categoryRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("id") Long id) {
        log.info("Category Controller: Request to getCategoryById : {}", id);
        return new ResponseEntity<>(categoryService.getCategoryById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Category Controller: Request to getAllCategories - page: {}, size: {}", page, size);
        return new ResponseEntity<>(categoryService.getAllCategories(page, size), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable("id") Long id, @RequestBody CategoryRequestDTO categoryRequestDTO) {
        log.info("Category Controller: Request to updateCategory with id: {} - {}", id, categoryRequestDTO);
        return new ResponseEntity<>(categoryService.updateCategory(id, categoryRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategoryById(@PathVariable("id") Long id) {
        log.info("Category Controller: Request to deleteCategoryById : {}", id);
        categoryService.deleteCategoryById(id);
        return  ResponseEntity.noContent().build();
    }


}
