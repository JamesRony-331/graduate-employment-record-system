package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import java.util.List;

public interface SysRoleService
{
    Long add(SysRolePO role);
    SysRolePO selectById(Long id);
    List<SysRolePO> selectAll();
    PageResult<SysRolePO> selectByPage(int pageNum, int pageSize);
    /** 更新非空业务字段。 */
    void update(SysRolePO role);
    /** 已绑定账号的角色不允许删除，可通过update禁用。 */
    void delete(Long id);
}
