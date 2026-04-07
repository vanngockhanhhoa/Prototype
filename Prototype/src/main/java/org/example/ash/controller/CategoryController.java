package org.example.ash.controller;

import lombok.AllArgsConstructor;
import org.example.ash.dto.CategoryDTO;
import org.example.ash.dto.request.CategoryRequest;
import org.example.ash.entity.oracle.Category;
import org.example.ash.mapper.CategoryMapper;
import org.example.ash.repository.oracle.ICategoryRepo;
import org.example.ash.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ICategoryRepo categoryRepo;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity getAllCategories(){
        return ResponseEntity.ok(categoryMapper.toListDto(categoryService.getAllCategories()));
    }

    @GetMapping("{id}")
    public ResponseEntity getDetail(@PathVariable("id") Long id){
        var result = categoryMapper.toDto(categoryRepo.findById(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping()
    public ResponseEntity create(@RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.createCategory(request));
    }
}
