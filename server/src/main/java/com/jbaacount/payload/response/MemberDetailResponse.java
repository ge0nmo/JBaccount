package com.jbaacount.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class MemberDetailResponse
{
    private Long id;

    private String nickname;

    private String email;

    @JsonProperty("profileImage")
    private String url;

    private int score;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm")
    private LocalDateTime createdAt;
    private String timeInfo;
    private Boolean isAdmin;


    @QueryProjection
    public MemberDetailResponse(Long id, String nickname, String email, String url, int score, LocalDateTime createdAt)
    {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.url = url;
        this.score = score;
        this.createdAt = createdAt;
    }
}
