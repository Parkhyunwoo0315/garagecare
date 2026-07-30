package com.hyunu.garagecare.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSignUpRequest {

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Size(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상, 30자 이하여야 합니다.")
    private String password;


}