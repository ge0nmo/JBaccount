package com.jbaacount.category.service;

import com.jbaacount.board.entity.Board;
import com.jbaacount.board.repository.BoardRepository;
import com.jbaacount.category.dto.request.CategoryPatchDto;
import com.jbaacount.category.dto.response.CategoryInfoForResponse;
import com.jbaacount.category.entity.Category;
import com.jbaacount.category.repository.CategoryRepository;
import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.global.service.AuthorizationService;
import com.jbaacount.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService
{
    private final CategoryRepository categoryRepository;
    private final BoardRepository boardRepository;
    private final AuthorizationService authorizationService;

    public Category createCategory(Category category, Long boardId, Member currentMember)
    {
        authorizationService.isAdmin(currentMember);
        Board board = getBoard(boardId);

        category.addBoard(board);

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, CategoryPatchDto request, Member currentMember)
    {
        authorizationService.isAdmin(currentMember);

        Category category = getCategory(categoryId);
        Optional.ofNullable(request.getBoardId())
                .ifPresent(boardId ->
                {
                    Board board = getBoard(boardId);
                    category.addBoard(board);
                    category.changeCategoryAuthority(request.isAdminOnly());
                });

        Optional.ofNullable(request.getName())
                .ifPresent(name -> category.updateName(name));

        Optional.ofNullable(request.isAdminOnly())
                .ifPresent(isAdminOnly -> category.changeCategoryAuthority(isAdminOnly));



        return category;
    }

    @Transactional(readOnly = true)
    public CategoryInfoForResponse getCategoryResponseInfo(Long categoryId, Pageable pageable)
    {
        return categoryRepository.getCategoryInfo(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CategoryInfoForResponse> getAllCategoryResponseInfo(Long boardId, Pageable pageable)
    {
        return categoryRepository.getAllCategoryInfo(boardId, pageable);
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long categoryId)
    {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new BusinessLogicException(ExceptionMessage.CATEGORY_NOT_FOUND));
    }

    @Transactional

    public void deleteCategory(Long categoryId, Member currentMember)
    {
        authorizationService.isAdmin(currentMember);

        categoryRepository.deleteById(categoryId);
    }

    private Board getBoard(Long boardId)
    {
        return boardRepository.findById(boardId).orElseThrow();
    }
}