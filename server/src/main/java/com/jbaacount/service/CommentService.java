package com.jbaacount.service;

import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.mapper.CommentMapper;
import com.jbaacount.model.Comment;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.model.type.CommentType;
import com.jbaacount.payload.request.CommentCreateRequest;
import com.jbaacount.payload.request.CommentUpdateRequest;
import com.jbaacount.payload.response.CommentChildrenResponse;
import com.jbaacount.payload.response.CommentParentResponse;
import com.jbaacount.payload.response.CommentResponseForProfile;
import com.jbaacount.payload.response.CommentSingleResponse;
import com.jbaacount.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService
{
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UtilService authService;
    private final MemberService memberService;
    private final VoteService voteService;

    @Transactional
    public CommentSingleResponse saveComment(CommentCreateRequest request, Member member)
    {
        Post post = postService.getPostById(request.getPostId());
        Comment comment = CommentMapper.INSTANCE.toCommentEntity(request);
        Member currentMember = memberService.getMemberById(member.getId());


        comment.addPost(post);
        comment.addMember(currentMember);
        if(request.getParentCommentId() != null)
        {
            Comment parent = getComment(request.getParentCommentId());
            checkIfPostHasExactComment(post, parent);
            if(parent.getParent() != null)
            {
                throw new RuntimeException();
            }

            comment.addParent(parent);
            comment.setType(CommentType.CHILD_COMMENT.getCode());
        }

        if(post.getMember() != currentMember)
        {
            currentMember.getScoreByComment();
        }

        Comment savedComment = commentRepository.save(comment);

        return getCommentSingleResponse(savedComment.getId(), member);
    }

    @Transactional
    public CommentSingleResponse updateComment(CommentUpdateRequest request, Long commentId, Member currentMember)
    {
        Comment comment = getComment(commentId);
        authService.isTheSameUser(comment.getMember().getId(), currentMember.getId());

        Optional.ofNullable(request.getText())
                .ifPresent(text -> comment.updateText(text));

        return getCommentSingleResponse(commentId, currentMember);
    }

    public Comment getComment(Long commentId)
    {
        return commentRepository.findById(commentId).orElseThrow(() -> new BusinessLogicException(ExceptionMessage.COMMENT_NOT_FOUND));
    }


    public List<CommentParentResponse> getAllCommentByPostId(Long postId, Member member)
    {
        log.info("postid = {}", postId);

        List<Comment> parentCommentsByPostId = commentRepository.findParentCommentsByPostId(postId, CommentType.PARENT_COMMENT.getCode());
        var parentList  = CommentMapper.INSTANCE.toCommentParentResponseList(parentCommentsByPostId);

        for (CommentParentResponse parent : parentList)
        {
            parent.setVoteStatus(checkVoteStatus(member, parent.getId()));

            for (CommentChildrenResponse child : parent.getChildren())
            {
                child.setVoteStatus(checkVoteStatus(member, child.getId()));
            }
        }

        return parentList;
    }

    public Page<CommentResponseForProfile> getAllCommentsForProfile(Long memberId, Pageable pageable)
    {
        return commentRepository.getAllCommentsForProfile(memberId, pageable);
    }

    public CommentSingleResponse getCommentSingleResponse(Long commentId, Member member)
    {
        Comment comment = getComment(commentId);

        boolean voteStatus = checkVoteStatus(member, commentId);
        log.info("comment 내용 {}", comment.getText() );

        return CommentMapper.INSTANCE.toCommentSingleResponse(comment, voteStatus);
    }

    @Transactional
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

    public boolean checkVoteStatus(Member member, Long commentId)
    {
        if(member == null)
            return false;

        log.info("member id = {}", member.getId());
        log.info("comment id = {}", commentId);
        return voteService.existByMemberAndComment(member.getId(), commentId);
    }

}
