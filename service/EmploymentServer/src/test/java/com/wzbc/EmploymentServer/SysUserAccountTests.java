package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.mapping.SqlCommandType;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Service.impl.SysUserServiceImpl;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;

import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

class SysUserAccountTests
{
    private final MemoryMapper mapper = new MemoryMapper();
    private final SysUserServiceImpl service = new SysUserServiceImpl(mapper, null, null, null, null);

    private void register()
    {
        SysUserRegisterDTO dto = new SysUserRegisterDTO();
        dto.setUsername(" teacher ");
        dto.setPassword("password123");
        dto.setCounselorId(7L);
        service.register(dto);
    }

    private SysUserLoginDTO credentials(String password)
    {
        SysUserLoginDTO dto = new SysUserLoginDTO();
        dto.setUsername("teacher");
        dto.setPassword(password);
        return dto;
    }

    @Test
    void registrationHashesPasswordAndLoginReturnsScopedIdentity()
    {
        register();
        assertNotEquals("password123", mapper.row.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("password123", mapper.row.getPassword()));
        var response = service.login(credentials("password123"));
        assertEquals(1L, TokenUserDTO.parseSysUserId(response.getToken()));
        assertEquals(7L, response.getUserMsgVO().getCounselorId());
        assertThrows(BusinessException.class, () -> service.login(credentials("wrongpassword")));
    }

    @Test
    void studentAndStaffTokensCannotBeUsedInterchangeably()
    {
        String student = TokenUserDTO.generateAccountToken("student01", 2L, "STUDENT", 1L);
        String staff = TokenUserDTO.generateSysUserToken("teacher", 1L);
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseSysUserId(student));
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseUuid(staff));
        int signature = staff.lastIndexOf('.') + 1;
        String tampered = staff.substring(0, signature) + (staff.charAt(signature) == 'A' ? 'B' : 'A') +
                          staff.substring(signature + 1);
        assertThrows(BusinessException.class, () -> TokenUserDTO.parseSysUserId(tampered));
    }

    @Test
    void passwordUpdateRejectsOldPasswordAndAcceptsNewOne()
    {
        register();
        SysUserUpdateDTO dto = new SysUserUpdateDTO();
        dto.setPassword("newpassword123");
        dto.setRealName("张老师");
        service.updateInfo(1L, dto);
        assertThrows(BusinessException.class, () -> service.login(credentials("password123")));
        assertEquals("张老师", service.login(credentials("newpassword123")).getUserMsgVO().getRealName());
    }

    @Test
    void deletedAccountCannotLoginUpdateOrReregister()
    {
        register();
        Long tokenId = TokenUserDTO.parseSysUserId(service.login(credentials("password123")).getToken());
        service.softDelete(tokenId);
        assertNotNull(mapper.row);
        assertEquals(0, mapper.row.getStatus());
        assertThrows(BusinessException.class, () -> service.login(credentials("password123")));
        assertThrows(BusinessException.class, () -> service.updateInfo(tokenId, new SysUserUpdateDTO()));
        assertThrows(BusinessException.class, this::register);
    }

    @Test
    void usernameCannotBeChangedAfterRegistration()
    {
        register();
        SysUserUpdateDTO dto = new SysUserUpdateDTO();
        dto.setUsername("renamed");
        assertThrows(BusinessException.class, () -> service.updateInfo(1L, dto));
    }

    @Test
    void mapperLoadsAndSoftDeleteUsesUpdate() throws Exception
    {
        Configuration config = new Configuration();
        try (InputStream input = getClass().getResourceAsStream("/mapper/SysUserMapper.xml"))
        {
            assertNotNull(input);
            new XMLMapperBuilder(input, config, "mapper/SysUserMapper.xml", config.getSqlFragments()).parse();
        }
        var statement = config.getMappedStatement(SysUserMapper.class.getName() + ".softDelete");
        assertEquals(SqlCommandType.UPDATE, statement.getSqlCommandType());
        String sql = statement.getBoundSql(Map.of("id", 1L)).getSql();
        assertTrue(sql.contains("status = 0"));
        assertTrue(sql.contains("WHERE id = ? AND status = 1"));
    }

    /** 替代外部数据库；密码、身份校验和业务分支均执行实际代码。 */
    private static class MemoryMapper implements SysUserMapper
    {
        private SysUserPO row;
        public int updateStudentRealName(Long uuid, String name)
        {
            return 0;
        }
        public String selectCounselorNameByUserId(Long userId)
        {
            return null;
        }
        public int selectCount()
        {
            return row == null ? 0 : 1;
        }
        public List<SysUserPageDTO> SelectByPage(PageQuery query)
        {
            if (row == null || query.getStart() > 0 || query.getPageSize() <= 0)
                return List.of();
            return List.of(CopyTools.copy(row, SysUserPageDTO.class));
        }
        public SysUserPO selectById(Long id)
        {
            return row != null && row.getId().equals(id) ? row : null;
        }
        public SysUserPO selectByIdForUpdate(Long id)
        {
            return selectById(id);
        }
        public SysUserPO selectByUsername(String username)
        {
            return row != null && row.getUsername().equals(username) ? row : null;
        }
        public int insert(SysUserPO user)
        {
            user.setId(1L);
            row = CopyTools.copy(user, SysUserPO.class);
            return 1;
        }
        public int update(SysUserPO user)
        {
            if (selectById(user.getId()) == null || row.getStatus() != 1)
                return 0;
            if (user.getUsername() != null)
                row.setUsername(user.getUsername());
            if (user.getPassword() != null)
                row.setPassword(user.getPassword());
            if (user.getCounselorId() != null)
                row.setCounselorId(user.getCounselorId());
            if (user.getRealName() != null)
                row.setRealName(user.getRealName());
            return 1;
        }
        public int upgradePassword(Long id, String oldPassword, String newPassword)
        {
            if (selectById(id) == null || !row.getPassword().equals(oldPassword))
                return 0;
            row.setPassword(newPassword);
            row.setPasswordAlgorithm("BCRYPT");
            return 1;
        }
        public int softDelete(Long id)
        {
            if (selectById(id) == null || row.getStatus() != 1)
                return 0;
            row.setStatus(0);
            return 1;
        }
    }
}
