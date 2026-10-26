package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.MajorPO;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface MajorMapper
{
    @Select("SELECT id, college_id AS collegeId, major_name AS majorName FROM major WHERE id = #{id}")
    MajorPO selectById(@Param("id") Long id);
    @Select("SELECT id, college_id AS collegeId, major_name AS majorName FROM major ORDER BY college_id, id")
    List<MajorPO> selectAll();
    @Insert("INSERT INTO major(college_id,major_name,created_at,updated_at) VALUES(#{collegeId},#{majorName},CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MajorPO record);
    @Select("SELECT id, college_id AS collegeId, major_name AS majorName FROM major WHERE id=#{id} FOR UPDATE")
    MajorPO selectByIdForUpdate(@Param("id") Long id);
    @Select("SELECT COUNT(*) FROM major")
    int selectCount();
    @Select("SELECT id, college_id AS collegeId, major_name AS majorName FROM major ORDER BY id DESC LIMIT #{pageSize} OFFSET #{start}")
    List<MajorPO> selectByPage(@Param("start") int start, @Param("pageSize") int pageSize);
    @Update("UPDATE major SET college_id=#{collegeId},major_name=#{majorName},updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int update(MajorPO record);
    @Delete("DELETE FROM major WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    @Select("SELECT (SELECT COUNT(*) FROM class_info WHERE major_id=#{id}) + (SELECT COUNT(*) FROM student WHERE major_id=#{id})")
    int countReferences(@Param("id") Long id);
}
