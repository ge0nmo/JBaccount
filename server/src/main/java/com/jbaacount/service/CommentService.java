package com.jbaacount.service;

import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.mapper.CommentMapper;
import com.jbaacount.model.Comment;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.payload.request.CommentCreateRequest;
import com.jbaacount.payload.request.CommentPatchDto;
import com.jbaacount.payload.response.CommentMultiResponse;
import com.jbaacount.payload.response.CommentResponseForProfile;
import com.jbaacount.payload.response.CommentSingleResponse;
import com.jbaacount.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class CommentService
{
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final AuthorizationService authService;
    private final MemberService memberService;
    private final VoteService voteService;

    public CommentSingleResponse saveComment(CommentCreateRequest request, Long postId, Long parentId, Member member)
    {
        Post post = postService.getPostById(postId);
        Comment comment = CommentMapper.INSTANCE.toCommentEntity(request);
        Member currentMember = memberService.getMemberById(member.getId());


        comment.addPost(post);
        comment.addMember(currentMember);
        if(parentId != null)
        {
            Comment parent = getComment(parentId);
            checkIfPostHasExactComment(post, parent);
            if(parent.getParent() != null)
            {
                throw new RuntimeException();
            }

            comment.addParent(parent);
        }

        if(post.getMember() != currentMember)
        {
            currentMember.getScoreByComment();
        }

        Comment savedComment = commentRepository.save(comment);

        return getCommentSingleResponse(savedComment.getId(), member);
    }

    public CommentSingleResponse updateComment(CommentPatchDto request, Long commentId, Member currentMember)
    {
        Comment comment = getComment(commentId);
        authService.isTheSameUser(comment.getMember().getId(), currentMember.getId());

        Optional.ofNullable(request.getText())
                .ifPresent(text -> comment.updateText(text));

        return getCommentSingleResponse(commentId, currentMember);
    }

    @Transactional(readOnly = true)
    public Comment getComment(Long commentId)
    {
        return commentRepository.findById(commentId).orElseThrow();
    }


    @Transactional(readOnly = true)
    public List<CommentMultiResponse> getAllComments(Long postId, Member member)
    {
        var list = commentRepository.getAllComments(postId);

        for (CommentMultiResponse response : list)
        {
            if(member != null)
                response.setVoteStatus(checkVoteStatus(member.getId(), response.getId()));
        }

        return list;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Pageable pageable)
    {
        return commentRepository.getAllCommentsForProfile(memberId, pageable);
    }

    public CommentSingleResponse getCommentSingleResponse(Long commentId, Member member)
    {
        Comment comment = getComment(commentId);
        boolean voteStatus = false;

        if(member != null)
            voteStatus = checkVoteStatus(member.getId(), commentId);

        return CommentMapper.INSTANCE.toCommentSingleResponse(comment, voteStatus);
    }

    public void deleteComment(Long commentId, Member currentMember)
    {
        Comment comment = getComment(commentId);

        authService.checkPermission(comment.getMember().getId(), currentMember);

        if(comment.getChildren().isEmpty())
            commentRepository.deleteById(commentId);

        else
            comment.deleteComment();
    }

    private void checkIfPostHasExactComment(Post post, Comment comment)
    {
        if(comment.getPost() != post)
            throw new BusinessLogicException(ExceptionMessage.POST_NOT_FOUND);
    }

    public boolean checkVoteStatus(Long memberId, Long commentId)
    {
        if(memberId == null)
            return false;

        return voteService.existByMemberAndComment(memberId, commentId);
    }

}