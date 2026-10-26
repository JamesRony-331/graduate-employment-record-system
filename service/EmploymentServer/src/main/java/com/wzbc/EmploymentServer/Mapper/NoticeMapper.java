package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.NoticePO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NoticeMapper
{
    NoticePO selectPublishedById(@Param("id") Long id);
    int countPublished();
    List<NoticePO> selectPublishedPage(@Param("start") int start, @Param("pageSize") int pageSize);
    int insert(NoticePO record);
    NoticePO selectById(@Param("id") Long id);
    NoticePO selectByIdForUpdate(@Param("id") Long id);
    List<NoticePO> selectByPage(@Param("start") int start, @Param("pageSize") int pageSize);
    int selectCount();
    int update(NoticePO record);
    int deleteById(@Param("id") Long id);
}
