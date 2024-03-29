package com.jbaacount.service;

import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.model.Comment;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.model.Vote;
import com.jbaacount.repository.CommentRepository;
import com.jbaacount.repository.PostRepository;
import com.jbaacount.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class VoteService
{
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public boolean votePost(Member currentMember, Long postId)
    {
        Post post = findByPostId(postId);
        Optional<Vote> optionalVote = voteRepository.findByMemberAndPost(currentMember, post);

        if(optionalVote.isPresent())
        {
            voteRepository.delete(optionalVote.get());
            post.downVote();
            post.getMember().getScoreByVote(-2);
            return false;
        }

        else
        {
            Vote savedVote = voteRepository.save(new Vote(currentMember, post));
            post.getMember().getScoreByVote(2);
            post.upVote();
            log.info("voted post = {}", savedVote.getPost().getTitle());
            return true;
        }
    }

    @Transactional
    public boolean voteComment(Member currentMember, Long commentId)
    {
        Comment comment = findByCommentId(commentId);
        Optional<Vote> optionalVote = voteRepository.findByMemberAndComment(currentMember, comment);

        if(optionalVote.isPresent())
        {
            voteRepository.delete(optionalVote.get());
            comment.getMember().getScoreByVote(-2);

            return false;
        }

        else
        {
            voteRepository.save(new Vote(currentMember, comment));
            comment.getMember().getScoreByVote(2);

            return true;
        }
    }

    @Transactional
    public void deleteVoteByPostId(Long postId)
    {
        voteRepository.deleteVoteByPostId(postId);
    }

    @Transactional
    public void deleteVoteByCommentId(Long commentId)
    {
        voteRepository.deleteVoteByCommentId(commentId);
    }

    public boolean existByMemberAndPost(Member member, Post post)
    {
        return voteRepository.existsVoteByMemberAndPost(member, post);
    }

    public boolean existByMemberAndComment(Long memberId, Long commentId)
    {
        return voteRepository.existsVoteByMemberIdAndCommentId(memberId, commentId);
    }

    private Post findByPostId(Long postId)
    {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionMessage.POST_NOT_FOUND));
    }

    private Comment findByCommentId(Long commentId)
    {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionMessage.COMMENT_NOT_FOUND));
    }
}
