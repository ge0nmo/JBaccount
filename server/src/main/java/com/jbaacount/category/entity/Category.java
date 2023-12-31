package com.jbaacount.category.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jbaacount.board.entity.Board;
import com.jbaacount.global.audit.BaseEntity;
import com.jbaacount.post.entity.Post;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
public class Category extends BaseEntity
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JsonProperty("isAdminOnly")
    private Boolean isAdminOnly;

    @Column(nullable = false)
    private Long orderIndex;

    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
    private List<Post> posts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    public void addBoard(Board board)
    {
        if(this.board != null)
            this.board.getCategories().remove(this);

        this.board = board;
        board.getCategories().add(this);
    }

    @Builder
    public Category(String name, boolean isAdminOnly)
    {
        this.name = name;
        this.isAdminOnly = isAdminOnly;
    }

    public void updateName(String name)
    {
        this.name = name;
    }

    public void changeCategoryAuthority(Boolean isAdminOnly)
    {
        if(board.getIsAdminOnly() == true)
            this.isAdminOnly = true;

        else
            this.isAdminOnly = isAdminOnly;
    }

    public void updateOrderIndex(Long orderIndex)
    {
        this.orderIndex = orderIndex;
    }
}
