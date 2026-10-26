package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.WorkSkillPO;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface WorkSkillMapper
{
    @Select("SELECT id,name FROM work_skill WHERE LOCATE(#{keyword},name)>0 ORDER BY id LIMIT #{size} OFFSET #{offset}")
    List<WorkSkillPO> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
    @Select("SELECT COUNT(*) FROM work_skill WHERE LOCATE(#{keyword},name)>0")
    int count(@Param("keyword") String keyword);
    @Select("SELECT id,name FROM work_skill WHERE id=#{id}")
    WorkSkillPO detail(Long id);
    @Select("SELECT COUNT(*) FROM work_skill WHERE name=#{name}")
    int exists(String name);
    @Select("SELECT COUNT(*) FROM work_compare WHERE JSON_CONTAINS(skills,JSON_QUOTE(#{name}))")
    int references(String name);
    @Insert("INSERT INTO work_skill(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkSkillPO row);
    @Update("UPDATE work_skill SET name=#{name} WHERE id=#{id}")
    int update(WorkSkillPO row);
    @Delete("DELETE FROM work_skill WHERE id=#{id}")
    int delete(Long id);
}
