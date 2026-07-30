package com.hyunu.garagecare.member.controller;

@Controller
@RequiredArgsConstrutor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("form", new MemberSignUpRequest());
        return "member/signup-form";
    }

    @PostMapping("signup")
    public String signUp(
            @Valid @ModelAttribute("form") MemberSignUpRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return "member/sign-form";
        }

        memberService.signUp(request);
        return "redirect:/member/signup/complete";
    }

    @GetMapping("/sign/complate")
    public String signUpComplate() {
        return "member/signup-complate";
    }
}