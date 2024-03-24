package com.jbaacount.repository;

import com.jbaacount.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom
{

    @Query("SELECT b FROM Board b WHERE b.parent.id = :parentId")
    List<Board> findBoardByParentBoardId(@Param("parentId") Long parentId);

    @Query("SELECT Count(b) FROM Board b WHERE b.parent.id = :parentId")
    Integer countChildrenByParentId(@Param("parentId") Long parentId);

    @Query("SELECT Count(b) FROM Board b WHERE b.parent IS NULL")
    Integer countParent();

    @Query("SELECT b.id FROM Board b WHERE b.parent.id = :boardId order by b.id desc")
    List<Long> findBoardIdListByParentId(@Param("boardId") Long boardId);
}
