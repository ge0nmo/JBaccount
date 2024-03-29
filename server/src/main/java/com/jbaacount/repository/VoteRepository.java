package com.jbaacount.repository;

import com.jbaacount.model.Comment;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long>
{
    @Query("select v from Vote v where v.member = :member and v.post = :post")
    Optional<Vote> findByMemberAndPost(@Param("member") Member member, @Param("post") Post post);

    @Query("select v from Vote v where v.member = :member and v.comment = :comment")
    Optional<Vote> findByMemberAndComment(@Param("member") Member member, @Param("comment") Comment comment);

    Optional<Vote> findByMemberIdAndPostId(@Param("memberId") Long memberId, @Param("postId") Long postId);

    Optional<Vote> findByMemberIdAndCommentId(@Param("memberId") Long memberId, @Param("commentId") Long commentId);

    void deleteVoteByPostId(@Param("postId") Long postId);

    void deleteVoteByCommentId(@Param("commentId") Long commentId);

    boolean existsVoteByMemberAndPost(Member member, Post post);

    boolean existsVoteByMemberIdAndCommentId(@Param("memberId") Long memberId, @Param("commentId") Long commentId);

}
