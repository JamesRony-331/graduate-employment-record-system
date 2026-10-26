package com.wzbc.EmploymentServer.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 每次请求检查当前账号状态，避免统一账号后身份串用及禁用后旧Token继续生效。 */
public class AccountAuthenticationFilter extends OncePerRequestFilter
{
    private final SysUserMapper mapper;
    private final ObjectMapper json;
    private final UserMapper students;
    public AccountAuthenticationFilter(SysUserMapper mapper, UserMapper students, ObjectMapper json)
    {
        this.mapper = mapper;
        this.students = students;
        this.json = json;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        return "OPTIONS".equals(request.getMethod()) ||
                ("POST".equals(request.getMethod()) &&
                 "/sys-user/login".equals(request.getRequestURI().substring(request.getContextPath().length())));
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        SysUserPO account;
        try
        {
            String header = request.getHeader("Authorization");
            if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7))
            {
                throw new BusinessException(401, "请先登录");
            }
            String token = header.substring(7).trim();
            if (token.isEmpty())
            {
                throw new BusinessException(401, "请先登录");
            }
            var identity = TokenUserDTO.verify(token);
            account = mapper.selectById(identity.accountId());
            if (account == null || !Integer.valueOf(1).equals(account.getStatus()))
            {
                throw new BusinessException(401, "账号不存在或已禁用");
            }
            if (!Objects.equals(account.getAccountType(), identity.accountType()))
            {
                throw new BusinessException(401, "账号身份已变更，请重新登录");
            }
            if (!Objects.equals(account.getUsername(), identity.username()))
                throw new BusinessException(401, "账号信息已变更，请重新登录");
            if ("STUDENT".equals(account.getAccountType()))
            {
                if (!Objects.equals(account.getStudentUuid(), identity.studentUuid()) ||
                    account.getCounselorId() != null)
                    throw new BusinessException(401, "学生关联已变更，请重新登录");
                if (students.selectById(account.getStudentUuid()) == null)
                    throw new BusinessException(401, "关联学生资料不存在");
            }
            else if (account.getStudentUuid() != null)
            {
                throw new BusinessException(401, "账号关联异常，请联系管理员");
            }
        }
        catch (BusinessException e)
        {
            SecurityContextHolder.clearContext();
            response.setStatus(e.getCode() == null ? 401 : e.getCode());
            response.setContentType("application/json;charset=UTF-8");
            json.writeValue(response.getWriter(), Map.of("code", response.getStatus(), "info", e.getMessage()));
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + account.getAccountType()))));
        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
    }
}
