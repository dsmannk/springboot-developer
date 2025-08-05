# springboot-developer
스프링부트3로 백엔드 입문하기


## 스프링 시큐리티로 로그인/로그아웃, 회원 가입 구현하기

#### 스프링 시큐리티는 필터 기반으로 동작하며 다양한 필터들로 나누어져 있어, 각 필터에서 인증, 인가와 관련된 작업을 처리합니다.
#### SecurityContextPersistenceFilter부터 시작해서 아래로 내려가며 FilterSecurityInterceptor까지 순서대로 필터를 거칩니다.

### SecurityFilterChain

- SecurityContextPersistenceFilter
  : SecurityContextRepository에서 SecurityContext(접근 주체와 인증에 대한 정보를 담고 있는 객체)를 가져오거나 저장하는 역할을 합니다.
  : 인증 연결 - SucurityContextRepository -> SecurityContextHolder(인증 외부) -> SecurityContext -> Authentication

- Logoutfilter
  : 설정된 로그아웃 URL로 오는 요청을 확인해 해당 사용자를 로그아웃 처리합니다.
  : 인증 연결 - LogoutSuccess Handler

- UsernamePasswordAuthenticationFilter
  : 인증 관리자입니다. 폼 기반 로그인을 할 때 사용되는 필터로 아이디, 패스워드 데이터를 파싱해 인증 요청을 위임합니다. 인증이 성공하면 
  : AuthenticationSuccessHandler를, 인증에 실패하면 AuthenticationFailureHandler를 실행합니다.
  : 인증 연결 - AuthenticationManager, AuthenticationSuccessHandler, AuthenticationFailureHandler, SessionAuthenticationStrategy -> SessionRegistry (인증 외부)

- DefaultLoginPageGeneratingFilter
  : 사용자가 로그인 페이지를 따로 지정하지 않았을 때 기본으로 설정하는 로그인 페이지 관련 필터입니다.

- BasicAuthenticationFilter
  : 요청 헤더에 있는 아이디와 패스워드를 파싱해서 인증 요청을 위임합니다. 인증이 성공하면 AuthenticationSuccessHandler를, 인증에 실패하면 AuthenticationFailureHandler를 실행합니다.
  : 인증 연결 - AuthenticationManager, 인가 연결 - AuthenticationEntryPoint

- RequestCacheAwareFilter
  : 로그인 성공 후, 관련 있는 캐시 요청이 있는지 확인하고 캐시 요청을 처리해줍니다. 예를 들어 로그인하지 않은 상태로 방문했던 페이지를 기억해두었다가 
  : 로그인 이후에 그 페이지로 이동 시켜줍니다.
  : 

- SecurityContextHolderAwareRequestFilter
  : HttpServletRequest 정보를 감쌉니다. 필터 체인 상의 다음 필터들에게 부가 정보를 제공하기 위해 사용합니다.

- AnonymousAuthenticationFilter
  : 필터가 호출되는 시점까지 인증되지 않았다면 익명 사용자 전용 객체인 AnonymousAuthentication을 만들어 SecurityContext에 넣어줍니다.

- SessionManagementFilter
  : 인증된 사용자와 관련된 세션 관련 작업을 진행합니다. 세션 변조 방지 전략을 설정하고, 유효하지 않은 세션에 대한 처리를 하고, 세션 생성 전략을
  : 세우는 등의 작업을 처리합니다.
  : 인증 연결 - SucurityContextRepository -> SecurityContextHolder(인증 외부) -> SecurityContext -> Authentication
  : 인증 연결 - AuthenticationFailureHandler, SessionAuthenticationStrategy -> SessionRegistry (인증 외부)

- ExceptionTranslationFilter
  : 요청을 처리하는 중에 발생할 수 있는 예외를 위임하거나 전달합니다.
  : 인가 연결 - AuthenticationEntryPoint, AccessDeniedEntryPoint, AccessDeniedHandler

- FilterSecurityInterceptor 
  : 접근 결정 관리자입니다. AccessDecisionManager로 권한 부여 처리를 위임함으로써 접근 제어 결정을 쉽게 해줍니다. 이 과정에서 이미 사용자가 인증되어 있으므로
  : 유효한 사용자인지도 알 수 있습니다. 즉, 인가 관련 설정을 할 수 있습니다.
  : 인증 연결 - AuthenticationManager, 인가 연결 - AccessDecisionManager -> DecisionVoters, SecurityMetadataSource

