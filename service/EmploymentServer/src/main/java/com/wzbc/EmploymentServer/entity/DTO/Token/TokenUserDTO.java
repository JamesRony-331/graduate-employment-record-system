package com.wzbc.EmploymentServer.entity.DTO.Token;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class TokenUserDTO
{
    private static final String SECRET_KEY = loadSecretKey();

    private static String loadSecretKey()
    {
        String key = System.getProperty("jwt.secret");
        if (key == null || key.isBlank())
            key = System.getenv("JWT_SECRET");
        if (key == null || key.isBlank())
        {
            java.nio.file.Path envFile = java.nio.file.Path.of(".env");
            if (java.nio.file.Files.isRegularFile(envFile))
            {
                java.util.Properties properties = new java.util.Properties();
                try (var reader = java.nio.file.Files.newBufferedReader(envFile))
                {
                    properties.load(reader);
                    key = properties.getProperty("JWT_SECRET");
                }
                catch (java.io.IOException e)
                {
                    throw new IllegalStateException("无法读取本地 JWT 配置", e);
                }
            }
        }
        if (key == null || key.isBlank())
            throw new IllegalStateException("请配置 JWT_SECRET，或设置 jwt.secret 系统属性");
        if (io.jsonwebtoken.io.Decoders.BASE64.decode(key).length < 32)
            throw new IllegalStateException("JWT_SECRET 必须为至少 32 字节密钥的 Base64 编码");
        return key;
    }

    public static String generateAccountToken(String username, Long id, String type, Long studentUuid)
    {
        if (id == null || id <= 0 || !("STUDENT".equals(type) || "STAFF".equals(type)))
            throw new BusinessException("账号身份不合法");
        if ("STUDENT".equals(type) && (studentUuid == null || studentUuid <= 0))
            throw new BusinessException("学生关联不合法");
        if ("STAFF".equals(type) && studentUuid != null)
            throw new BusinessException("教职工不能携带学生身份");
        return Jwts.builder()
                .claim("identityVersion", 2)
                .claim("accountType", type)
                .claim("sysUserId", id)
                .claim("studentUuid", studentUuid)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000L))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    public static String generateSysUserToken(String username, Long id)
    {
        return generateAccountToken(username, id, "STAFF", null);
    }
    /** 已验签的身份快照，过滤器一次解析后复用，不能用未验证的JWT载荷鉴权。 */
    public record VerifiedIdentity(Long accountId, String accountType, Long studentUuid, String username)
    {
    }
    public static VerifiedIdentity verify(String token)
    {
        Claims c = claims(token);
        String type = (String)c.get("accountType");
        return new VerifiedIdentity(positiveId(c, "sysUserId"), type,
                                    "STUDENT".equals(type) ? positiveId(c, "studentUuid") : null, c.getSubject());
    }
    private static Claims claims(String token)
    {
        try
        {
            var jwt = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            if (!"HS256".equals(jwt.getHeader().getAlgorithm()))
                throw new BusinessException(401, "登录签名算法不合法");
            Claims c = jwt.getBody();
            if (c.getExpiration() == null || !c.getExpiration().after(new Date()) || c.getIssuedAt() == null ||
                c.getIssuedAt().after(new Date()) || !c.getExpiration().after(c.getIssuedAt()) ||
                c.getSubject() == null || c.getSubject().isBlank())
                throw new BusinessException(401, "登录信息缺少有效时间或账号");
            if (!(c.get("identityVersion") instanceof Number version) || !"2".equals(version.toString()) ||
                !("STUDENT".equals(c.get("accountType")) || "STAFF".equals(c.get("accountType"))))
                throw new BusinessException(401, "账号体系已更新，请重新登录");
            positiveId(c, "sysUserId");
            if ("STUDENT".equals(c.get("accountType")))
                positiveId(c, "studentUuid");
            else if (c.get("studentUuid") != null)
                throw new BusinessException(401, "账号身份声明不合法");
            return c;
        }
        catch (ExpiredJwtException e)
        {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        catch (BusinessException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new BusinessException(401, "登录信息无效，请重新登录");
        }
    }
    private static Long positiveId(Claims c, String key)
    {
        Object value = c.get(key);
        if (!(value instanceof Integer || value instanceof Long) || ((Number)value).longValue() <= 0)
            throw new BusinessException(401, "登录信息缺少有效整数身份ID");
        return ((Number)value).longValue();
    }
    public static Long parseAccountId(String token)
    {
        return positiveId(claims(token), "sysUserId");
    }
    public static Long parseSysUserId(String token)
    {
        Claims c = claims(token);
        if (!"STAFF".equals(c.get("accountType")))
            throw new BusinessException(403, "请使用教职工账号");
        return positiveId(c, "sysUserId");
    }
    public static Long parseUuid(String token)
    {
        Claims c = claims(token);
        if (!"STUDENT".equals(c.get("accountType")))
            throw new BusinessException(403, "请使用学生账号");
        return positiveId(c, "studentUuid");
    }
    public static String parseAccountType(String token)
    {
        return (String)claims(token).get("accountType");
    }
    public static String parseToken(String token)
    {
        return claims(token).getSubject();
    }
    public static boolean isTokenExpired(String token)
    {
        try
        {
            claims(token);
            return false;
        }
        catch (BusinessException e)
        {
            return true;
        }
    }
}
