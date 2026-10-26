package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.BasicKnowledgePO;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface BasicKnowledgeMapper
{
    @Insert("INSERT INTO basic_knowledge(title,answer,status) VALUES(#{title},#{answer},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BasicKnowledgePO row);
    @Update("UPDATE basic_knowledge SET title=#{title},answer=#{answer},status=#{status} WHERE id=#{id}")
    int update(BasicKnowledgePO row);
    @Delete("DELETE FROM basic_knowledge WHERE id=#{id}")
    int delete(Long id);
    @Select("SELECT id,title,answer,status FROM basic_knowledge WHERE id=#{id}")
    BasicKnowledgePO detail(Long id);
    @Select("SELECT COUNT(*) FROM basic_knowledge")
    int count();
    @Select("SELECT id,title,answer,status FROM basic_knowledge ORDER BY id DESC LIMIT #{size} OFFSET #{offset}")
    List<BasicKnowledgePO> page(@Param("offset") int offset, @Param("size") int size);
    /** 获取所有已生效知识，供后续 AI 服务组装知识上下文使用。 */
    @Select("SELECT id,title,answer,status FROM basic_knowledge WHERE status=1 ORDER BY id ASC")
    List<BasicKnowledgePO> selectAllEnabled();
}
