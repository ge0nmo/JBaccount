package com.jbaacount.controller;

import com.jbaacount.payload.request.CommentPatchDto;
import com.jbaacount.payload.request.CommentPostDto;
import com.jbaacount.payload.response.CommentMultiResponse;
import com.jbaacount.payload.response.CommentResponseForProfile;
import com.jbaacount.payload.response.CommentSingleResponse;
import com.jbaacount.model.Comment;
import com.jbaacount.mapper.CommentMapper;
import com.jbaacount.service.CommentService;
import com.jbaacount.global.dto.PageDto;
import com.jbaacount.global.dto.SingleResponseDto;
import com.jbaacount.model.Member;
import com.jbaacount.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping
@RestController
public class CommentController
{
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final MemberService memberService;


    @PostMapping("/{post-id}/comment")
    public ResponseEntity saveComment(@PathVariable("post-id") @Positive Long postId,
                                      @RequestBody @Valid CommentPostDto request,
                                      @AuthenticationPrincipal Member currentMember)
    {
        Comment comment = commentMapper.postToComment(request);
        Member member = memberService.getMemberById(currentMember.getId());
        Comment savedComment = commentService.saveComment(comment, postId, request.getParentId(), member);
        CommentSingleResponse response = commentMapper.commentToResponse(savedComment, member);

        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/comment/{comment-id}")
    public ResponseEntity updateComment(@RequestBody @Valid CommentPatchDto request,
                                        @PathVariable("comment-id") @Positive Long commentId,
                                        @AuthenticationPrincipal Member currentMember)
    {
        Comment updatedComment = commentService.updateComment(request, commentId, currentMember);
        CommentSingleResponse response = commentMapper.commentToResponse(updatedComment, currentMember);

        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @GetMapping("/comment/{comment-id}")
    public ResponseEntity getComment(@PathVariable("comment-id") @Positive Long commentId,
                                     @AuthenticationPrincipal Member currentMember)
    {
        Comment comment = commentService.getComment(commentId);
        CommentSingleResponse response = commentMapper.commentToResponse(comment, currentMember);

        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @GetMapping("/{post-id}/comments")
    public ResponseEntity getAllComments(@PathVariable("post-id") @Positive Long postId,
                                         @AuthenticationPrincipal Member currentMember)
    {
        List<CommentMultiResponse> response = commentService.getAllComments(postId, currentMember);

        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/profile/{member-id}/comments")
    public ResponseEntity<PageDto<CommentResponseForProfile>> getAllCommentsForProfile(@PathVariable("member-id") @Positive Long memberId,
                                                   @PageableDefault(size = 8) Pageable pageable)
    {
        PageDto<CommentResponseForProfile> response = commentService.getAllCommentsForProfile(memberId, pageable.previousOrFirst());

        return new ResponseEntity(response, HttpStatus.OK);
    }


    @DeleteMapping("/comment/{comment-id}")
    public ResponseEntity deleteComment(@PathVariable("comment-id") @Positive Long commentId,
                                        @AuthenticationPrincipal Member currentMember)
    {
        commentService.deleteComment(commentId, currentMember);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
