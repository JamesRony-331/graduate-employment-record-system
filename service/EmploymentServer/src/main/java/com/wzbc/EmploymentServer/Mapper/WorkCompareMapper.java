package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;
import com.wzbc.EmploymentServer.Mapper.typehandler.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface WorkCompareMapper
{
    @Select("SELECT * FROM work_compare WHERE id=#{id}")
    @Results(id = "workCompare",
             value =
             {
                 @Result(column = "company_name", property = "companyName")
                 , @Result(column = "company_contact", property = "companyContact"),
                         @Result(column = "id", property = "id", id = true),
                         @Result(column = "created_at", property = "createdAt"),
                         @Result(column = "updated_at", property = "updatedAt"),
                         @Result(column = "education", property = "education"),
                         @Result(column = "major", property = "major"),
                         @Result(column = "experience", property = "experience"),
                         @Result(column = "skills", property = "skills", typeHandler = WorkStringListHandler.class),
                         @Result(column = "categories", property = "categories",
                                 typeHandler = WorkStringListHandler.class),
                         @Result(column = "cities", property = "cities", typeHandler = WorkStringListHandler.class),
                         @Result(column = "job_type", property = "jobType"),
                         @Result(column = "minimum_salary", property = "minimumSalary"),
                         @Result(column = "expected_salary", property = "expectedSalary"),
                         @Result(column = "work_mode", property = "workMode"),
                         @Result(column = "rest_days", property = "restDays"),
                         @Result(column = "travel", property = "travel"),
                         @Result(column = "outsourcing", property = "outsourcing"),
                         @Result(column = "priority", property = "priority"),
                         @Result(column = "other", property = "other"),
                         @Result(column = "strict", property = "strict", typeHandler = WorkStrictHandler.class)
             })
    WorkComparePO
    detail(@Param("id") Long id);
    /** 查询全部岗位，不分页，按ID倒序返回。 */
    @Select("SELECT * FROM work_compare ORDER BY id DESC")
    @ResultMap("workCompare")
    List<WorkComparePO> selectAll();

    @Select("SELECT * FROM work_compare ORDER BY id DESC LIMIT #{size} OFFSET #{offset}")
    @ResultMap("workCompare")
    List<WorkComparePO> page(@Param("offset") int offset, @Param("size") int size);
    @Select("SELECT COUNT(*) FROM work_compare")
    int count();
    @Insert("INSERT INTO work_compare (company_name, company_contact, education, major, experience, skills, categories, cities, job_type, minimum_salary, expected_salary, work_mode, rest_days, travel, outsourcing, priority, other, strict) VALUES (#{companyName}, #{companyContact}, #{education}, #{major}, #{experience}, #{skills,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, #{categories,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, #{cities,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, #{jobType}, #{minimumSalary}, #{expectedSalary}, #{workMode}, #{restDays}, #{travel}, #{outsourcing}, #{priority}, #{other}, #{strict,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStrictHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkComparePO record);
    @Update("UPDATE work_compare SET company_name=#{companyName}, company_contact=#{companyContact}, education=#{education}, major=#{major}, experience=#{experience}, skills=#{skills,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, categories=#{categories,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, cities=#{cities,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStringListHandler}, job_type=#{jobType}, minimum_salary=#{minimumSalary}, expected_salary=#{expectedSalary}, work_mode=#{workMode}, rest_days=#{restDays}, travel=#{travel}, outsourcing=#{outsourcing}, priority=#{priority}, other=#{other}, strict=#{strict,typeHandler=com.wzbc.EmploymentServer.Mapper.typehandler.WorkStrictHandler}, updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id}")
    int update(WorkComparePO record);
    @Delete("DELETE FROM work_compare WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
