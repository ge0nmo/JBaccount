package com.jbaacount.member.repository;

import com.jbaacount.global.dto.SliceDto;
import com.jbaacount.global.utils.PaginationUtils;
import com.jbaacount.member.dto.response.MemberInfoForResponse;
import com.jbaacount.member.dto.response.MemberResponseDto;
import com.jbaacount.member.dto.response.MemberRewardResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.jbaacount.member.entity.QMember.member;
import static com.jbaacount.post.entity.QPost.post;
import static com.jbaacount.vote.entity.QVote.vote;

@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom
{
    private final JPAQueryFactory query;
    private final PaginationUtils paginationUtils;

    @Override
    public SliceDto<MemberResponseDto> findAllMembers(String keyword, Long memberId, Pageable pageable)
    {
        log.info("===findAllMembers in repository===");
        List<MemberResponseDto> memberDto = query
                .select(memberToResponse())
                .from(member)
                .leftJoin(member.file)
                .where(ltMemberId(memberId))
                .where(checkEmailKeyword(keyword))
                .where(checkNicknameKeyword(keyword))
                .orderBy(member.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        log.info("list size = {}", memberDto.size());

        Slice<MemberResponseDto> slice = paginationUtils.toSlice(pageable, memberDto);

        return new SliceDto<>(memberDto, slice);
    }

    @Override
    public List<MemberRewardResponse> memberResponseForReward(LocalDateTime now)
    {
        LocalDateTime startMonth = LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0);
        LocalDateTime endMonth = startMonth.plusMonths(1);

        List<Tuple> memberListTuple = query
                .select(member.id, member.nickname, member.score)
                .from(member)
                .leftJoin(member.posts, post)
                .leftJoin(post.votes, vote)
                .where(post.createdAt.between(startMonth, endMonth))
                .where(post.member.email.ne("mike@ticonsys.com"))
                .groupBy(member.id)
                .orderBy(
                        member.score.desc(), //점수 기준
                        post.count().desc(), //해당 월에 작성한 게시글 기준
                        post.voteCount.sum().desc(), //해당 월에 받은 투표 개수 기준
                        member.posts.size().desc(), //그 동안의 총 개시물 갯수
                        member.createdAt.asc() //가입 날짜 오래 된 순
                )
                .limit(3)
                .fetch();

        List<MemberRewardResponse> responses = memberListTuple.stream()
                .map(tuple -> new MemberRewardResponse(tuple.get(member.id), tuple.get(member.nickname), tuple.get(member.score)))
                .collect(Collectors.toList());

        return responses;
    }

    private ConstructorExpression<MemberResponseDto> memberToResponse()
    {
        StringExpression url = new CaseBuilder()
                .when(member.file.isNotNull())
                .then(member.file.url)
                .otherwise((String) null);

        log.info("===memberToResponse===");
        return Projections.constructor(MemberResponseDto.class,
                member.id,
                member.nickname,
                member.email,
                url,
                member.createdAt,
                member.modifiedAt);
    }

    private ConstructorExpression extractMemberForReward()
    {
        return Projections.constructor(MemberInfoForResponse.class,
                member.id,
                member.nickname,
                member.score);
    }


    private BooleanExpression ltMemberId(Long memberId)
    {
        return memberId != null ? member.id.lt(memberId) : null;
    }


    private BooleanExpression checkEmailKeyword(String keyword)
    {
         return keyword != null ? member.email.lower().contains(keyword.toLowerCase()) : null;
    }

    private BooleanExpression checkNicknameKeyword(String keyword)
    {
        return keyword != null ? member.nickname.lower().contains(keyword.toLowerCase()) : null;
    }

}
