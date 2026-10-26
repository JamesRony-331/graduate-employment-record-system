package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.EmploymentAuditRecordPO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface EmploymentAuditRecordMapper
{
    int insert(EmploymentAuditRecordPO record);
    EmploymentAuditRecordPO selectById(@Param("id") Long id);
    List<EmploymentAuditRecordPO> selectByFilingId(@Param("filingId") Long filingId);
    int selectCount();
    List<EmploymentAuditRecordPO> selectByPage(@Param("start") int start, @Param("pageSize") int pageSize);
    int update(EmploymentAuditRecordPO record);
    int deleteById(@Param("id") Long id);
    int countFiling(@Param("filingId") Long filingId);
}
