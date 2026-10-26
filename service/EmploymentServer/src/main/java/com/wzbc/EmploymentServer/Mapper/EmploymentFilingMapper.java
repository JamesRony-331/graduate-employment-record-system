package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.EmploymentFilingPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmploymentFilingMapper
{
    EmploymentFilingPO selectByIdForUpdate(@Param("id") Long id);
    int updateReviewStatus(@Param("id") Long id, @Param("version") int version, @Param("fromStatus") int fromStatus,
                           @Param("toStatus") int toStatus);

    List<EmploymentFilingPO> selectManagedFilings(@Param("userId") Long userId, @Param("status") int status,
                                                  @Param("department") boolean department);

    List<EmploymentFilingPO> selectLastFilingByStudentUuid(@Param("studentUuid") Long studentUuid);

    int InsertEmploymentFiling(EmploymentFilingPO filing);

    int DeleteEmploymentFiling(@Param("id") Long id);

    /** 必须传入 id 和当前 version；返回 0 表示记录不存在或版本冲突。 */
    int UpdateEmploymentFiling(EmploymentFilingPO filing);

    List<EmploymentFilingPO> SelectEmploymentFilingById(@Param("id") Long id);

    List<EmploymentFilingPO> SelectAllEmploymentFiling();

    List<EmploymentFilingPO> SelectEmploymentFilingByStudent(@Param("studentUuid") Long studentUuid);

    List<EmploymentFilingPO> SelectEmploymentFilingByClass(@Param("classId") Long classId);
}
