package com.jbaacount.post.repository;

import com.jbaacount.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom
{
    @Query(value = "select * from post p where p.title like %:keyword%", nativeQuery = true)
    Optional<Post> findByTitle(String keyword);
}
