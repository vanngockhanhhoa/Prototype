package org.example.ash.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ash.dto.CategoryDTO;
import org.example.ash.dto.request.CategoryRequest;
import org.example.ash.entity.oracle.Category;
import org.example.ash.mapper.CategoryMapper;
import org.example.ash.repository.oracle.ICategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService {

    private final ICategoryRepo categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(CategoryRequest category) {
        return categoryRepository.save(categoryMapper.toEntity(category));
    }
}
