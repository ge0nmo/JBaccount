package com.jbaacount.category.repository;

import com.jbaacount.category.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryRepositoryCustom
{
    List<CategoryResponseDto> findAllCategories(Long boardId);

    long countCategory(Long boardId);

    void bulkUpdateOrderIndex(long start, long end, int num, long boardId);

    Long findTheBiggestOrderIndex(long boardId);
}
