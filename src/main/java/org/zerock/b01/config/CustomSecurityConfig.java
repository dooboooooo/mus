package org.zerock.b01.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.zerock.b01.security.CustomUserDetailsService;
import org.zerock.b01.security.handler.Custom403Handler;
import org.zerock.b01.security.handler.CustomSocialLoginSuccessHandler;

import javax.sql.DataSource;

@Configuration
@Log4j2
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final DataSource dataSource; // 데이터 소스 주입
    private final UserDetailsService userDetailsService; // 사용자 상세 정보 서비스 주입
    private final CustomUserDetailsService customUserDetailsService; // CustomUserDetailsService 주입

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { // BCryptPasswordEncoder 빈 생성
        return new BCryptPasswordEncoder();
    }
    // 추가된 부분 시작: DaoAuthenticationProvider 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    // 추가된 부분 끝

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("adminPass"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    // InMemoryUserDetailsManager를 위한 DaoAuthenticationProvider 설정
    @Bean
    public DaoAuthenticationProvider inMemoryAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(inMemoryUserDetailsManager());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // CustomUserDetailsService를 위한 DaoAuthenticationProvider 설정
    @Bean
    public DaoAuthenticationProvider databaseAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("----------------Security Config----------------------");

        http.formLogin(form -> {
            form.loginPage("/member/login") //커스텀 로그인 페이지
                    .successHandler((request, response, authentication) -> {
                        response.sendRedirect("/"); // 성공
                    })
                    .failureUrl("/member/login/error"); // 실패
        });

        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());

        http.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout")) // 로그아웃 요청 URL 설정
                .logoutSuccessUrl("/member/login") // 성공 시 이동
                .invalidateHttpSession(true)); // 세션 삭제

        http.rememberMe(httpSecurityRememberMeConfigurer -> { // 아이디 저장
            httpSecurityRememberMeConfigurer
                    .key("12345678") // 토큰 키 설정
                    .tokenRepository(persistentTokenRepository()) // 토큰 저장소 설정
                    .userDetailsService(userDetailsService) // 사용자 상세 정보 서비스 설정
                    .tokenValiditySeconds(60 * 60 * 24 * 30); // 토큰 유효 (30일)
        });

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> { // 예외 처리 설정
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler()); // 접근 거부 핸들러 설정
        });

        http.oauth2Login(httpSecurityOAuth2LoginConfigurer -> { // 소셜 로그인 설정
            httpSecurityOAuth2LoginConfigurer.loginPage("/member/login"); // 로그인 페이지 URL 설정
            httpSecurityOAuth2LoginConfigurer.successHandler(authenticationSuccessHandler()); // 로그인 성공 핸들러 설정
        });

        // 최신 버전에 맞게 URL 패턴 권한 설정
        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/admin/list","/admin/register").hasRole("ADMIN");
            authorize.anyRequest().permitAll();
        });

        http.authenticationProvider(inMemoryAuthenticationProvider());
        http.authenticationProvider(databaseAuthenticationProvider());

        return http.build(); // 보안 필터 체인 빌드하여 반환
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() { // 접근 거부 핸들러 빈 생성
        return new Custom403Handler(); // 커스텀 접근 거부 핸들러 반환kk
    }

    @Bean

    public AuthenticationSuccessHandler authenticationSuccessHandler() { // 인증 성공 핸들러 빈 생성
        return new CustomSocialLoginSuccessHandler(passwordEncoder()); // 커스텀 소셜 로그인 성공 핸들러 반환
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // 웹 보안 커스터마이저 빈 생성
        log.info("------------web configure-------------------");
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 정적 리소스 요청 무시 설정
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() { // 지속적 토큰 저장소 빈 생성
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl(); // JDBC 기반 토큰 저장소 생성
        repo.setDataSource(dataSource); // 데이터 소스 설정
        return repo; // 토큰 저장소 반환
    }

}
