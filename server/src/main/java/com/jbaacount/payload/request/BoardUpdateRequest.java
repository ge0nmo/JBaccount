package com.jbaacount.payload.request;

import com.jbaacount.global.validation.notspace.NotSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardUpdateRequest
{
    @NotBlank
    private Long boardId;
    @NotSpace
    @Size(max = 15, message = "게시판 제목은 최대 15자까지 입력 가능합니다.")
    private String name;
    private Boolean isAdminOnly;

    @NotBlank
    private Long orderIndex;
    private Boolean isDeleted;
    private List<CategoryUpdateRequest> category = new ArrayList<>();
}