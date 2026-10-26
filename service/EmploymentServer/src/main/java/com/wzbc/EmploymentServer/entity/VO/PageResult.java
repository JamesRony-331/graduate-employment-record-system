package com.wzbc.EmploymentServer.entity.VO;

import java.util.List;

/** 分页结果，pageNum 从1开始。 */
public record PageResult<T>(List<T> records, int pageNum, int pageSize, int totalCount, int total)
{
}
