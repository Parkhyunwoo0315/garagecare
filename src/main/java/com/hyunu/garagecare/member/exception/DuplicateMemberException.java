package com.hyunu.garagecare.member.exception;

public class DuplicateMemberException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "이미 가입된 이메일입니다. ";

    public DuplicateMemberException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateMemberException(String message) {
        super(message);
    }
}