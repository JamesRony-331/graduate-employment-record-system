package com.wzbc.EmploymentServer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class TokenValidationTests
{
    private String signed(Object id, String subject, Date expiration) throws Exception
    {
        var key = TokenUserDTO.class.getDeclaredField("SECRET_KEY");
        key.setAccessible(true);
        var builder = Jwts.builder()
                              .claim("identityVersion", 2)
                              .claim("accountType", "STAFF")
                              .claim("sysUserId", id)
                              .setSubject(subject)
                              .setIssuedAt(new Date());
        if (expiration != null)
            builder.setExpiration(expiration);
        return builder.signWith(SignatureAlgorithm.HS256, (String)key.get(null)).compact();
    }
    @Test
    void rejectsMissingExpiration() throws Exception
    {
        String token = signed(1, "teacher", null);
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId(token));
    }
    @Test
    void rejectsFractionalIdInsteadOfTruncatingIt() throws Exception
    {
        String token = signed(1.5, "teacher", new Date(System.currentTimeMillis() + 60000));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId(token));
    }
    @Test
    void rejectsBlankSubject() throws Exception
    {
        String token = signed(1, " ", new Date(System.currentTimeMillis() + 60000));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId(token));
    }
    @Test
    void rejectsExpiredUnsignedAndTamperedTokens() throws Exception
    {
        String expired = signed(1, "teacher", new Date(System.currentTimeMillis() - 60000));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId(expired));
        String valid = TokenUserDTO.generateSysUserToken("teacher", 1L);
        String[] parts = valid.split("\\.");
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sysUserId\":99}".getBytes(StandardCharsets.UTF_8));
        assertThrows(BusinessException.class,
                     () -> TokenUserDTO.parseAccountId(parts[0] + "." + payload + "." + parts[2]));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId(parts[0] + "." + parts[1] + "."));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseAccountId("not-a-token"));
        assertEquals(1L, TokenUserDTO.parseAccountId(valid));
    }
}
