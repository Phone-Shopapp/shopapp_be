package com.project.shopapp.responses;

import lombok.*;

import java.util.List;

@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CategoryListResponse {
    private List<CategoryResponse> categoryResponses;
    private int totalPages;
}
