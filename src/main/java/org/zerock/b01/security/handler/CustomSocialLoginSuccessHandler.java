package org.zerock.b01.security.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.zerock.b01.security.dto.MemberSecurityDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Log4j2
@RequiredArgsConstructor
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        log.info("----------------------------------------------------------");
        log.info("CustomLoginSuccessHandler onAuthenticationSuccess ..........");
        log.info(authentication.getPrincipal());

        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();
        // 현재 사용자의 인증 정보 Principal을 MemberSecurityDTO로 전달, Spring Security에서는 UserDetails를 상속(타입)한 객체가 Principal로 사용

        String encodedPw = memberSecurityDTO.getM_pw();

        //소셜로그인이고 회원의 패스워드가 1111이라면
        if (memberSecurityDTO.isM_social()
                && (memberSecurityDTO.getM_pw().equals("1111")
                ||  passwordEncoder.matches("1111", memberSecurityDTO.getM_pw())
        )) {
            log.info("Should Change Password");

            log.info("Redirect to Member Modify");
            response.sendRedirect("/");
            return;
        } else {
            response.sendRedirect("/board/list"); //소셜 로그인 암호가 1111이 아닌 경우
        }
    }
}