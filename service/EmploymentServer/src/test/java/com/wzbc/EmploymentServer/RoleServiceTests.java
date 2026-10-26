package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.impl.*;
import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTests
{
    private SqlSession session;
    private SysRoleServiceImpl roles;
    private SysUserRoleServiceImpl bindings;

    @BeforeEach
    void setup() throws Exception
    {
        var ds = new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL", "sa", "");
        var config = new Configuration(new Environment("test", new JdbcTransactionFactory(), ds));
        for (String name : new String[]
        {"SysRole", "SysUserRole", "SysUser"
        })
        {
            String resource = "mapper/" + name + "Mapper.xml";
            try (var input = getClass().getClassLoader().getResourceAsStream(resource))
            {
                new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
            }
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        sql("CREATE TABLE sys_role(id BIGINT AUTO_INCREMENT PRIMARY KEY, role_name VARCHAR(50) NOT NULL, role_code VARCHAR(50) NOT NULL UNIQUE, status TINYINT NOT NULL DEFAULT 1, sort_order INT NOT NULL DEFAULT 0, remark VARCHAR(500), created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL, updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL)");
        sql("CREATE TABLE sys_user_role(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL, UNIQUE(user_id,role_id))");
        sql("CREATE TABLE sys_user(id BIGINT PRIMARY KEY, username VARCHAR(50), password VARCHAR(100), password_algorithm VARCHAR(20), student_uuid BIGINT, account_type VARCHAR(20), counselor_id BIGINT, real_name VARCHAR(50), status INT, created_at TIMESTAMP, updated_at TIMESTAMP)");
        sql("INSERT INTO sys_user(id, username, status) VALUES(1,'staff',1),(2,'disabled',0)");
        var roleMapper = session.getMapper(SysRoleMapper.class);
        var bindingMapper = session.getMapper(SysUserRoleMapper.class);
        roles = new SysRoleServiceImpl(roleMapper, bindingMapper);
        bindings = new SysUserRoleServiceImpl(bindingMapper, session.getMapper(SysUserMapper.class), roleMapper);
    }

    @AfterEach
    void close()
    {
        if (session != null)
            session.close();
    }

    private void sql(String command) throws Exception
    {
        try (var statement = session.getConnection().createStatement())
        {
            statement.execute(command);
        }
    }

    private Long add(String code, int sort)
    {
        var role = new SysRolePO();
        role.setRoleName(code);
        role.setRoleCode(code);
        role.setSortOrder(sort);
        return roles.add(role);
    }

    @Test
    void roleCrudPaginationAndValidation()
    {
        Long teacher = add("TEACHER", 20);
        Long admin = add("ADMIN", 10);
        assertEquals(1, roles.selectById(teacher).getStatus());
        assertNotNull(roles.selectById(teacher).getCreatedAt());
        assertEquals(admin, roles.selectAll().getFirst().getId());
        var page = roles.selectByPage(2, 1);
        assertEquals(2, page.totalCount());
        assertEquals(teacher, page.records().getFirst().getId());
        var change = new SysRolePO();
        change.setId(teacher);
        change.setRemark("任课教师");
        roles.update(change);
        assertEquals("任课教师", roles.selectById(teacher).getRemark());
        assertEquals("TEACHER", roles.selectById(teacher).getRoleCode());
        assertThrows(BusinessException.class, () -> add("TEACHER", 0));
        change.setRoleCode("ADMIN");
        assertThrows(BusinessException.class, () -> roles.update(change));
        assertThrows(BusinessException.class, () -> roles.add(new SysRolePO()));
        roles.delete(admin);
        assertThrows(BusinessException.class, () -> roles.selectById(admin));
    }

    @Test
    void bindingIsIdempotentAndBlocksRoleDeletion()
    {
        Long role = add("TEACHER", 0);
        bindings.bind(1L, role);
        bindings.bind(1L, role);
        assertEquals(1, bindings.selectByUserId(1L).size());
        assertNotNull(bindings.selectByUserId(1L).getFirst().getCreatedAt());
        assertEquals(role, bindings.selectEnabledRolesByUserId(1L).getFirst().getId());
        assertThrows(BusinessException.class, () -> roles.delete(role));
        bindings.unbind(1L, role);
        bindings.unbind(1L, role);
        assertTrue(bindings.selectByUserId(1L).isEmpty());
        roles.delete(role);
    }

    @Test
    void bindingReplacesPreviousRoleAndPreservesItOnInvalidReplacement()
    {
        Long teacher = add("TEACHER", 0);
        Long admin = add("ADMIN", 1);
        bindings.bind(1L, teacher);
        bindings.bind(1L, admin);
        var current = bindings.selectByUserId(1L);
        assertEquals(1, current.size());
        assertEquals(admin, current.getFirst().getRoleId());
        Long bindingId = current.getFirst().getId();
        bindings.bind(1L, admin);
        assertEquals(bindingId, bindings.selectByUserId(1L).getFirst().getId());
        assertThrows(BusinessException.class, () -> bindings.bind(1L, 999L));
        assertEquals(admin, bindings.selectByUserId(1L).getFirst().getRoleId());
        roles.delete(teacher);
        assertThrows(BusinessException.class, () -> roles.delete(admin));
    }

    @Test
    void bindingNormalizesLegacyMultipleRoles() throws Exception
    {
        Long teacher = add("TEACHER", 0);
        Long admin = add("ADMIN", 1);
        sql("INSERT INTO sys_user_role(user_id,role_id) VALUES(1," + teacher + "),(1," + admin + ")");
        bindings.bind(1L, admin);
        assertEquals(1, bindings.selectByUserId(1L).size());
        assertEquals(admin, bindings.selectByUserId(1L).getFirst().getRoleId());
    }

    @Test
    void disabledAndMissingAccountsOrRolesAreRejected() throws Exception
    {
        Long role = add("ADMIN", 0);
        assertThrows(BusinessException.class, () -> bindings.bind(2L, role));
        assertThrows(BusinessException.class, () -> bindings.bind(999L, role));
        assertThrows(BusinessException.class, () -> bindings.bind(1L, 999L));
        bindings.bind(1L, role);
        var change = new SysRolePO();
        change.setId(role);
        change.setStatus(0);
        roles.update(change);
        assertTrue(bindings.selectEnabledRolesByUserId(1L).isEmpty());
        assertThrows(BusinessException.class, () -> bindings.bind(1L, role));
        change.setStatus(1);
        roles.update(change);
        sql("UPDATE sys_user SET status=0 WHERE id=1");
        session.clearCache();
        assertTrue(bindings.selectEnabledRolesByUserId(1L).isEmpty());
        bindings.unbind(1L, role);
        assertTrue(bindings.selectByUserId(1L).isEmpty());
    }
}
