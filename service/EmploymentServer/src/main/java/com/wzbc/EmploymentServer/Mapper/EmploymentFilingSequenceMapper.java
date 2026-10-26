package com.wzbc.EmploymentServer.Mapper;

import org.apache.ibatis.annotations.Param;

/** employment_filing_sequence 表的每日流水号操作。 */
public interface EmploymentFilingSequenceMapper
{
    /** 必须在同一事务中递增并读取，行锁保持到备案保存完成。 */
    int incrementApplicationSequence(@Param("applicationDate") String applicationDate);

    int selectApplicationSequence(@Param("applicationDate") String applicationDate);
}
