package com.jbaacount.post.mapper;

import com.jbaacount.member.dto.response.MemberInfoForResponse;
import com.jbaacount.member.entity.Member;
import com.jbaacount.member.mapper.MemberMapper;
import com.jbaacount.post.dto.request.PostPostDto;
import com.jbaacount.post.dto.response.PostResponseDto;
import com.jbaacount.post.entity.Post;
import com.jbaacount.vote.entity.Vote;
import com.jbaacount.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PostMapper
{
    private final MemberMapper memberMapper;
    private final VoteRepository voteRepository;

    public Post postDtoToPostEntity(PostPostDto request)
    {
        if(request == null)
            return null;

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return post;
    }

    public PostResponseDto postEntityToResponse(Post entity, Member currentMember)
    {
        MemberInfoForResponse memberResponse = memberMapper.memberToMemberInfo(currentMember);
        boolean voteStatus = false;

        if(currentMember != null)
        {
            Optional<Vote> vote = voteRepository.checkMemberVotedPostOrNot(currentMember, entity);
            voteStatus = vote.isPresent();
        }

        PostResponseDto response = PostResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .voteCount(entity.getVoteCount())
                .voteStatus(voteStatus)
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .member(memberResponse)
                .build();

        return response;
    }

}
