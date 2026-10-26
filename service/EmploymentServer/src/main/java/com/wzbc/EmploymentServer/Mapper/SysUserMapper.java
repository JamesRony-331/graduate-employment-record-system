package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.SysUserPageDTO;
import org.apache.ibatis.annotations.Param;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import java.util.List;

public interface SysUserMapper
{
    int selectCount();
    int updateStudentRealName(@Param("studentUuid") Long studentUuid, @Param("realName") String realName);

    String selectCounselorNameByUserId(@Param("userId") Long userId);
    /** 分页查询账号，包含禁用账号，按ID倒序排列。 */
    List<SysUserPageDTO> SelectByPage(@Param("PageQuery") PageQuery pageQuery);

    SysUserPO selectById(@Param("id") Long id);

    /** 锁定账号，用于串行化同一账号的角色变更。 */
    SysUserPO selectByIdForUpdate(@Param("id") Long id);

    /** 包含禁用账号，供登录状态判断和用户名查重使用。 */
    SysUserPO selectByUsername(@Param("username") String username);

    int insert(SysUserPO user);

    int update(SysUserPO user);

    int upgradePassword(@Param("id") Long id, @Param("oldPassword") String oldPassword,
                        @Param("newPassword") String newPassword);

    int softDelete(@Param("id") Long id);
}
