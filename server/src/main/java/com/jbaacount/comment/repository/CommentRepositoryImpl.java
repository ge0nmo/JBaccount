package com.jbaacount.comment.repository;

import com.jbaacount.comment.dto.response.CommentMultiResponse;
import com.jbaacount.comment.dto.response.CommentResponseForProfile;
import com.jbaacount.global.dto.PageDto;
import com.jbaacount.member.dto.response.MemberInfoForResponse;
import com.jbaacount.member.entity.Member;
import com.jbaacount.member.entity.QMember;
import com.jbaacount.vote.entity.Vote;
import com.jbaacount.vote.repository.VoteRepository;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static com.jbaacount.comment.entity.QComment.comment;
import static com.jbaacount.member.entity.QMember.member;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom
{
    private final JPAQueryFactory query;
    private final VoteRepository voteRepository;
    //private final PaginationUtils paginationUtils;

    @Override
    public List<CommentMultiResponse> getAllComments(Long postId, Member member)
    {
        List<CommentMultiResponse> listComments = query
                .select(extractCommentDto())
                .from(comment)
                .leftJoin(comment.parent)
                .join(comment.member, QMember.member)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.parent.id.asc().nullsFirst(), comment.createdAt.asc())
                .fetch();


        Map<Long, CommentMultiResponse> commentMap = new HashMap<>();
        List<CommentMultiResponse> parentComments = new ArrayList<>();

        for (CommentMultiResponse comment : listComments)
        {
            comment.setVoteStatus(checkMemberVotedCommentOrNot(member, comment.getId()));
            commentMap.put(comment.getId(), comment);

            if(comment.getParentId() == null)
                parentComments.add(comment);

            else
            {
                CommentMultiResponse parentComment = commentMap.get(comment.getParentId());
                if(parentComment != null)
                    parentComment.getChildren().add(comment);
            }

        }

        return parentComments;
    }

    /*@Override
    public SliceDto<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Long last, Pageable pageable)
    {
        List<CommentResponseForProfile> comments = query
                .select(extractAllCommentsForProfile())
                .from(comment)
                .where(ltCommentId(last))
                .where(comment.member.id.eq(memberId))
                .limit(pageable.getPageSize() + 1)
                .orderBy(comment.id.desc())
                .fetch();

        Slice<CommentResponseForProfile> slice = paginationUtils.toSlice(pageable, comments);

        return new SliceDto<>(comments, slice);
    }*/

    @Override
    public PageDto<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Pageable pageable)
    {
        List<CommentResponseForProfile> content = query
                .select(extractAllCommentsForProfile())
                .from(comment)
                .where(comment.member.id.eq(memberId))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .orderBy(comment.id.desc())
                .fetch();

        Long total = query
                .select(comment.count())
                .from(comment)
                .where(comment.member.id.eq(memberId))
                .fetchOne();

        PageImpl<CommentResponseForProfile> pageDto = new PageImpl<>(content, pageable, total);

        return new PageDto<>(pageDto);
    }


    ConstructorExpression<CommentResponseForProfile> extractAllCommentsForProfile()
    {
        return Projections.constructor(CommentResponseForProfile.class,
                comment.id,
                comment.post.id,
                comment.text,
                comment.voteCount,
                comment.createdAt);
    }

    ConstructorExpression<CommentMultiResponse> extractCommentDto()
    {
        return Projections.constructor(CommentMultiResponse.class,
                comment.id,
                comment.parent.id,
                comment.text,
                comment.voteCount,
                comment.createdAt,
                extractMemberInfo());
    }

    private ConstructorExpression<MemberInfoForResponse> extractMemberInfo()
    {
        return Projections.constructor(MemberInfoForResponse.class,
                member.id,
                member.nickname);
    }

    private boolean checkMemberVotedCommentOrNot(Member member, Long commentId)
    {
        if(member == null)
            return false;

        boolean voteStatus;
        Optional<Vote> optionalVote = voteRepository.checkMemberVotedCommentOrNot(member.getId(), commentId);
        voteStatus = optionalVote.isPresent();

        return voteStatus;
    }

    private BooleanExpression ltCommentId(Long commentId)
    {
        return commentId != null ? comment.id.lt(commentId) : null;
    }
}
