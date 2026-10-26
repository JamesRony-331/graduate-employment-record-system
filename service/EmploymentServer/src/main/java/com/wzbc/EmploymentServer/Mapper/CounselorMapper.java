package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.CounselorPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CounselorMapper
{
    CounselorPO selectById(@Param("id") Long id);

    CounselorPO selectByCounselorNo(@Param("counselorNo") String counselorNo);

    /** 查询全部辅导员，包含离职人员，按 ID 倒序排列。 */
    List<CounselorPO> selectAll();

    /** 根据分页参数查询辅导员，按 ID 倒序排列。 */
    List<CounselorPO> SelectByPage(@Param("PageQuery") PageQuery pageQuery);

    /** 新增辅导员，并回填自增 ID；未传状态时默认为在职。 */
    int insert(CounselorPO counselor);

    /** 按 ID 更新非空字段；status 可用于标记离职或恢复在职。 */
    int update(CounselorPO counselor);
}
