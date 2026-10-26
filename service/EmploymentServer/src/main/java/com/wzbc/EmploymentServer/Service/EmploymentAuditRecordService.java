package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.PO.EmploymentAuditRecordPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import java.util.List;

/** 内部审核记录管理；调用方负责审核权限及备案状态变更。 */
public interface EmploymentAuditRecordService
{
    Long add(EmploymentAuditRecordPO record);
    EmploymentAuditRecordPO selectById(Long id);
    List<EmploymentAuditRecordPO> selectByFilingId(Long filingId);
    PageResult<EmploymentAuditRecordPO> selectByPage(int pageNum, int pageSize);
    /** 完整更新记录内容，审核时间保留；不更新备案状态。 */
    void update(EmploymentAuditRecordPO record);
    /** 内部维护使用，不更新备案状态。 */
    void delete(Long id);
}
