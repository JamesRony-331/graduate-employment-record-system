package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.CounselorClassPO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CounselorClassMapper
{
    List<ClassPO> selectClassesByCounselorId(@Param("counselorId") Long counselorId);
    int insert(CounselorClassPO record);
    CounselorClassPO selectById(@Param("id") Long id);
    CounselorClassPO selectByIdForUpdate(@Param("id") Long id);
    List<CounselorClassPO> selectByPage(@Param("pageQuery") PageQuery query);
    int selectCount();
    int countDuplicate(@Param("counselorId") Long counselorId, @Param("classId") Long classId,
                       @Param("excludeId") Long excludeId);
    int update(CounselorClassPO record);
    int deleteById(@Param("id") Long id);
}
