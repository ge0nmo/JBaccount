package com.jbaacount.category.repository;

import com.jbaacount.category.dto.response.CategoryResponseDto;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.jbaacount.category.entity.QCategory.category;

@Slf4j
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom
{
    private final JPAQueryFactory query;

    @Override
    public List<CategoryResponseDto> findAllCategories(Long boardId)
    {
        return query
                .select(extractCategories())
                .from(category)
                .where(category.board.id.eq(boardId))
                .orderBy(category.orderIndex.asc())
                .fetch();
    }

    @Override
    public long countCategory(Long boardId)
    {
        return query
                .select(category.count())
                .from(category)
                .where(category.board.id.eq(boardId))
                .fetchOne();
    }

    private ConstructorExpression<CategoryResponseDto> extractCategories()
    {
        return Projections.constructor(CategoryResponseDto.class,
                category.id,
                category.name,
                category.isAdminOnly,
                category.orderIndex);
    }
}
