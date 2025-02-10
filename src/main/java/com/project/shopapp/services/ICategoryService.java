package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    Page<CategoryResponse> getAllCategories(PageRequest pageRequest);

//    List<Category> getAllCategories();

    Category updateCategory(long categoryId, CategoryDTO category);
    void deleteCategory(long id);
}
