package com.wzbc.EmploymentServer.Mapper;

import org.apache.ibatis.annotations.Update;

import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ClassMapper
{
    List<ClassPO> selectByDepartmentAdminUserId(@Param("userId") Long userId);
    @Update("UPDATE student SET major_id = #{majorId}, college_id = (SELECT c.college_id FROM class_info c WHERE c.id = #{classId}), updated_at = CURRENT_TIMESTAMP WHERE class_id = #{classId}")
    int syncStudentMajor(@Param("classId") Long classId, @Param("majorId") Long majorId);
    int insert(ClassPO record);
    List<ClassPO> selectByPage(@Param("pageQuery") PageQuery pageQuery);
    int selectCount();
    ClassPO selectByIdForUpdate(@Param("id") Long id);
    ClassPO selectById(@Param("id") Long id);
    int update(ClassPO record);
    int deleteById(@Param("id") Long id);
    int countReferences(@Param("id") Long id);
}
