package com.project.shopapp.controllers;

import com.project.shopapp.dtos.*;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryListResponse;
import com.project.shopapp.responses.CategoryResponse;
import com.project.shopapp.responses.UpdateCategoryResponse;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.utils.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    //Nếu tham số truyền vào là 1 object thì sao ? => Data Transfer Object = Request Object
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    CategoryResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_CATEGORY_FAILED))
                            .errors(errorMessages)
                            .build());
        }
        categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(CategoryResponse.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY))
                .build());
    }

//    //Hiện tất cả các categories
//    @GetMapping("")
//    public ResponseEntity<List<Category>> getAllCategories(
//            @RequestParam("page") int page,
//            @RequestParam("limit") int limit
//    ) {
//        List<Category> categories = categoryService.getAllCategories();
//        return ResponseEntity.ok(categories);
//    }

    @GetMapping("")
    public ResponseEntity<CategoryListResponse> getCategories(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
            Page<CategoryResponse> categoryPages = categoryService.getAllCategories(pageRequest);

            int totalPages = categoryPages.getTotalPages();
            List<CategoryResponse> categories = categoryPages.getContent();

            return ResponseEntity.ok(
                    CategoryListResponse.builder()
                            .categoryResponses(categories)
                            .totalPages(totalPages)
                            .build()
            );
        } catch (Exception e) {
            ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.badRequest().body(null);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        try {
            categoryService.updateCategory(id, categoryDTO);

            return ResponseEntity.ok(UpdateCategoryResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UpdateCategoryResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_CATEGORY_FAILED))
                    .build());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(CategoryResponse.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY))
                .build());
    }
}
