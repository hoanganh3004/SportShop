package com.library.sportshop.service;

import com.library.sportshop.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Page<Category> getAllCategories(String keyword, Pageable pageable);

    Category saveCategory(Category category);

    Optional<Category> getCategoryById(Integer id);

    void deleteCategory(Integer id);

    List<Category> findAll();
}
