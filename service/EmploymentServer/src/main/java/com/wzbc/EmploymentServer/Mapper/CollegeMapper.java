package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.CollegePO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CollegeMapper
{
    int insert(CollegePO record);
    List<CollegePO> selectByPage(@Param("pageQuery") PageQuery pageQuery);
    int selectCount();
    CollegePO selectById(@Param("id") Long id);
    int update(CollegePO record);
    int deleteById(@Param("id") Long id);
    int countReferences(@Param("id") Long id);
}
