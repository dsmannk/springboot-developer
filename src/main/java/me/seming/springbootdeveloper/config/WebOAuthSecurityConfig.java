package me.seming.springbootdeveloper.config;

import lombok.RequiredArgsConstructor;
import me.seming.springbootdeveloper.config.jwt.TokenProvider;
import me.seming.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import me.seming.springbootdeveloper.config.oauth.OAuth2SuccessHandler;
import me.seming.springbootdeveloper.config.oauth.OAuth2UserCustomService;
import me.seming.springbootdeveloper.repository.RefreshTokenRepository;
import me.seming.springbootdeveloper.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;

@RequiredArgsConstructor
@Controller
public class WebOAuthSecurityConfig {
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Bean
    public WebSecurityCustomizer configure() {

        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console())
                .requestMatchers("/img/**")
                .requestMatchers("/css/**")
                .requestMatchers("/js/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 경로 상수 도입으로 중복 제거 및 의도 명확화
        final String API_PATH = "/api/**";
        final String LOGIN_PATH = "/login";
        final String TOKEN_ENDPOINT = "/api/token";
        final AntPathRequestMatcher apiMatcher = new AntPathRequestMatcher(API_PATH);


        // 토큰 방식으로 인증을 하기 때문에 기존에 사용하던 폼 로그인, 세션 비활성화
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 헤더를 확인할 커스텀 필터 추가
                // 커스텀 토큰 인증 필터 삽입
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 토큰 재발급 URL은 인증 없이 접근 가능하도록 설정, 나머지 API URL은 인증 필요
                // 인가 규칙 정의 (Spring Security 6: authorizeHttpRequests)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(TOKEN_ENDPOINT).permitAll()
                        .requestMatchers(API_PATH).authenticated()
                        .anyRequest().permitAll())

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                                .loginPage(LOGIN_PATH)
                                // Authorization 요청과 관련된 상태 저장
                                .authorizationEndpoint(authorization -> authorization
                                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserCustomService))
                        // 인증 성공 시 실행할 핸들러
                        .successHandler(oAuth2SuccessHandler())
                )
                // /api로 시작하는 url인 경우 401 상태 코드를 반환하도록 예외 처리
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        ))
                .build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService
        );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
