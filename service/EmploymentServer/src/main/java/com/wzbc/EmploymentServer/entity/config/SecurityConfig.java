package com.wzbc.EmploymentServer.entity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.Filter.AccountAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Map;

@Configuration
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SysUserMapper mapper, UserMapper students,
                                                   ObjectMapper json) throws Exception
    {
        return http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AccountAuthenticationFilter(mapper, students, json),
                                 UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(errors -> errors.authenticationEntryPoint((req, res, e) ->
                {
                                                       res.setStatus(401);
                                                       res.setContentType("application/json;charset=UTF-8");
                                                       json.writeValue(res.getWriter(),
                                                                       Map.of("code", 401, "info", "请先登录"));
                                                   }).accessDeniedHandler((req, res, e) ->
                                                   {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    json.writeValue(res.getWriter(), Map.of("code", 403, "info", "当前账号无权访问此接口"));
                }))
                .authorizeHttpRequests(
                        auth
                        -> auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                   .permitAll()
                                   .requestMatchers(HttpMethod.POST, "/sys-user/login")
                                   .permitAll()
                                   .requestMatchers(HttpMethod.GET, "/sys-user/page", "/sys-user/SelectByPage")
                                   .hasRole("STAFF")
                                   .requestMatchers("/sys-user/register", "/sys-user/updateMsgAdmin", "/counselor/**")
                                   .hasRole("STAFF")
                                   .requestMatchers(HttpMethod.PUT, "/employment-filing/review/counselor",
                                                    "/employment-filing/review/department")
                                   .hasRole("STAFF")
                                   .requestMatchers(HttpMethod.GET, "/employment-filing/pending-review/*",
                                                    "/employment-filing/archived/*")
                                   .hasRole("STAFF")
                                   .requestMatchers("/sys-user/updateMsg", "/employment-filing/**")
                                   .hasRole("STUDENT")
                                   .requestMatchers(HttpMethod.GET, "/notice/published/page", "/notice/published/*")
                                   .hasAnyRole("STUDENT", "STAFF")
                                   .requestMatchers(HttpMethod.GET, "/sys-user/profile")
                                   .authenticated()
                                   .requestMatchers("/sys-user/updateInfo", "/sys-user/delete")
                                   .authenticated()
                                   .requestMatchers("/notice/**", "/student/**", "/major/**", "/class/**",
                                                    "/college/**", "/department-admin/**", "/counselor-class/**")
                                   .hasRole("STAFF")
                                   .requestMatchers(HttpMethod.POST, "/work-select", "/job-ai/prompt")
                                   .hasRole("STUDENT")
                                   .requestMatchers(HttpMethod.GET, "/work-skill/options")
                                   .hasAnyRole("STUDENT", "STAFF")
                                   .requestMatchers("/work-skill/**")
                                   .hasRole("STAFF")
                                   .requestMatchers(HttpMethod.GET, "/work-compare/page", "/work-compare/{id:[0-9]+}")
                                   .hasAnyRole("STUDENT", "STAFF")
                                   .requestMatchers("/work-compare/**")
                                   .hasRole("STAFF")
                                   .requestMatchers("/basic-knowledge/**")
                                   .hasRole("STAFF")
                                   .requestMatchers(HttpMethod.GET, "/service-entry/enabled")
                                   .authenticated()
                                   .requestMatchers("/service-entry/**")
                                   .hasRole("STAFF")
                                   .requestMatchers("/menu/mine")
                                   .authenticated()
                                   .requestMatchers("/menu/**")
                                   .hasRole("STAFF")
                                   .anyRequest()
                                   .denyAll())
                .build();
    }
}
