package com.hyunu.garagecare.member.controller;

@springBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("회원가입 화면 조회")
    void signupForm() throws Exception {
        mockMvc.perform(get("/members/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup-form"));
    }
}