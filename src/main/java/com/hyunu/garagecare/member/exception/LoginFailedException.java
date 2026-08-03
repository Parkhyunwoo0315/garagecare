package com.hyunu.garagecare.member.exception;

public class LoginFailedException extends RuntimeException {

    private static final String DEFALT_MESSAGE =
            "이메일 또는 비밀번호가 올바르지 않습니다.";

    public LoginFailedException() {
        super(DEFALT_MESSAGE);
    }
}
