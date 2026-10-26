package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wzbc.EmploymentServer.Exception.BusinessException;

import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.entity.Enum.ResponseCodeEnum;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ABaseController
{

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO<T> getSuccessResponseVO(T t)
    {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setMsg(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }
    protected String getUserId()
    {
        return TokenUserDTO.parseToken(getToken());
    }

    protected Long getUserUuid()
    {
        var user = currentAccount();
        if (!"STUDENT".equals(user.getAccountType()) || user.getStudentUuid() == null)
            throw new BusinessException(403, "请使用学生账号");
        return user.getStudentUuid();
    }

    protected Long getAccountId()
    {
        return currentAccount().getId();
    }

    private SysUserPO currentAccount()
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SysUserPO account))
            throw new BusinessException(401, "请先登录");
        return account;
    }

    protected String getToken()
    {
        HttpServletRequest request =
                ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank())
        {
            throw new BusinessException("请先登录");
        }
        token = token.trim();
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2)
        {
            token = token.substring(1, token.length() - 1).trim();
        }
        if (token.regionMatches(true, 0, "Bearer ", 0, 7))
        {
            token = token.substring(7).trim();
        }
        return token;
    }
}
