package com.jbaacount.comment.repository;

import com.jbaacount.comment.dto.response.CommentMultiResponse;
import com.jbaacount.comment.dto.response.CommentResponseForProfile;
import com.jbaacount.global.dto.PageDto;
import com.jbaacount.member.entity.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom
{
    List<CommentMultiResponse> getAllComments(Long postId, Member currentMember);

    //SliceDto<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Long last, Pageable pageable);

    PageDto<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Pageable pageable);
}
