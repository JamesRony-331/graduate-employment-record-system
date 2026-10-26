package com.wzbc.EmploymentServer.Mapper;

import org.apache.ibatis.annotations.Select;

import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper
{
    @Select("SELECT uuid FROM student WHERE uuid = #{uuid} FOR UPDATE")
    Long lockStudent(@Param("uuid") Long uuid);
    List<UserPO> selectByClassId(@Param("classId") Long classId);

    List<UserPO> selectByClassIds(@Param("classIds") List<Long> classIds);
    int countByClassIds(@Param("classIds") List<Long> classIds);
    List<UserPO> selectPageByClassIds(@Param("classIds") List<Long> classIds, @Param("start") int start,
                                      @Param("pageSize") int pageSize);
    int InsertUser(UserPO userPO);

    /** 按主键查询学生，未找到时返回 null。 */
    UserPO selectById(@Param("uuid") Long uuid);

    UserPO SelectUserByPhone(@Param("phone") String phone);

    /** 学号或身份证号任一匹配即返回；两个参数均为空时返回空列表。 */
    List<UserPO> SelectUserByStudentNoOrIdCard(@Param("studentNo") String studentNo, @Param("idCard") String idCard);

    int UpdateUser(UserPO userPO);

    List<UserPO> SelectUserByPage(@Param("PageQuery") PageQuery pageQuery);

    List<UserPO> SelectAllUser();

    int SelectUserCount();
}
