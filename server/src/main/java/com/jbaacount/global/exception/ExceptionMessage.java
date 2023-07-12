package com.jbaacount.global.exception;

import lombok.Getter;

public enum ExceptionMessage
{
    /**
     * member
     */
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXIST("이미 사용중인 이메일입니다."),
    NICKNAME_ALREADY_EXIST("이미 사용중인 닉네임입니다."),

    MEMBER_UNAUTHORIZED("권한이 없습니다."),

    /**
     * token
     */

    EXPIRED_TOKEN("이미 만료된 토큰입니다."),
    TOKEN_NOT_VALID("유효하지 않은 토큰입니다."),

    UNSUPPORTED_TOKEN("지원되지 않는 형식의 토큰입니다."),

    CLAIM_EMPTY("토큰의 정보를 찾을 수 없습니다."),

    INVALID_TOKEN_SIGNATURE("토큰의 서명이 유효하지 않습니다."),
    TOKEN_NOT_FOUND("해당 리프레시 토큰을 찾을 수 없습니다."),


    /**
     * post
     */

    POST_NOT_FOUND("해당 게시물을 찾을 수 없습니다.");


    @Getter
    private final String message;


    ExceptionMessage(String message)
    {
        this.message = message;
    }

    public String get()
    {
        return message;
    }
}
