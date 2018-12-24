package top.kaiccc.kai4boot.user.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author kaiccc
 * @date 2018-12-21 15:09
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserSecurityServiceImpl securityService;
    private final JwtAuthenticationFilter authenticationFilter;

    @Autowired
    public SecurityConfig(UserSecurityServiceImpl securityService, JwtAuthenticationFilter authenticationFilter) {
        this.securityService = securityService;
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(securityService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().and().csrf().disable();              //使用 JWT，关闭csrf防护;

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);    // 使用token 关闭session

        http.authorizeRequests()            //拦截页面
            .antMatchers(                   //静态资源 无授权访问
                    "/",
                    "/static/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v2/**",
                    "/h2web/**/**",
                    "/admin/user/login/**"
            ).permitAll()
            .anyRequest().authenticated();                                // 除上诉内容，全部页面都要验证

        // 添加JWT filter
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // 异常统一处理
        http.exceptionHandling().authenticationEntryPoint(getAuthenticationEntryPoint());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationEntryPoint getAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}