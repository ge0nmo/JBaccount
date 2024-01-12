package com.jbaacount.repository;

import com.jbaacount.payload.response.*;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.jbaacount.model.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom
{
    private final JPAQueryFactory query;

    @Override
    public Page<PostMultiResponse> getPostsByBoardId(Long boardId, String keyword, Pageable pageable)
    {
        List<PostMultiResponse> data = query
                .select(extractPostResponse())
                .from(post)
                .where(post.board.id.eq(boardId))
                .where(titleCondition(keyword))
                .orderBy(post.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> count = query
                .select(post.count())
                .from(post)
                .where(titleCondition(keyword))
                .where(post.board.id.eq(boardId));

        return PageableExecutionUtils.getPage(data, pageable, count::fetchOne);
    }

    @Override
    public Page<PostMultiResponse> getPostsByCategoryId(Long categoryId, String keyword, Pageable pageable)
    {
        List<PostMultiResponse> data = query
                .select(extractPostResponse())
                .from(post)
                .where(post.category.id.eq(categoryId))
                .where(titleCondition(keyword))
                .orderBy(post.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> count = query
                .select(post.count())
                .from(post)
                .where(titleCondition(keyword))
                .where(post.category.id.eq(categoryId));

        return PageableExecutionUtils.getPage(data, pageable, count::fetchOne);
    }

    @Override
    public Page<PostResponseForProfile> getPostsByMemberId(Long memberId, Pageable pageable)
    {
        List<PostResponseForProfile> content = query
                .select(extractPostsForProfile())
                .from(post)
                .where(post.member.id.eq(memberId))
                .orderBy(post.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> count = query
                .select(post.count())
                .from(post)
                .where(post.member.id.eq(memberId));


        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    private ConstructorExpression<PostResponseForProfile> extractPostsForProfile()
    {
        return Projections.constructor(PostResponseForProfile.class,
                post.id,
                post.title,
                post.createdAt);
    }

    private ConstructorExpression<PostMultiResponse> extractPostResponse()
    {
        return Projections.constructor(PostMultiResponse.class,
                getBoardInfo(),
                getCategoryInfo(),
                getMemberInfo(),
                post.id,
                post.title,
                post.content,
                post.comments.size(),
                post.createdAt);
    }

    private ConstructorExpression<BoardSimpleResponse> getBoardInfo()
    {
        return Projections.constructor(BoardSimpleResponse.class,
                post.board.id,
                post.board.name);
    }

    private ConstructorExpression<CategorySimpleResponse> getCategoryInfo()
    {
        return Projections.constructor(CategorySimpleResponse.class,
                post.category.id,
                post.category.name);
    }

    private ConstructorExpression<MemberSimpleResponse> getMemberInfo()
    {
        return Projections.constructor(MemberSimpleResponse.class,
                post.member.id,
                post.member.nickname);
    }


    private BooleanExpression titleCondition(String keyword)
    {
        return keyword != null ? post.title.lower().contains(keyword.toLowerCase()) : null;
    }

}