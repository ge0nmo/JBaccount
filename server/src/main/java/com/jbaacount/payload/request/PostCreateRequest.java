package com.jbaacount.payload.request;

import com.jbaacount.global.validation.notspace.NotSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostCreateRequest
{
    @NotSpace
    @Size(max = 50, message = "게시글 제목은 최대 50자까지 입력 가능합니다.")
    private String title;
    private String content;

    private Long boardId;
}
