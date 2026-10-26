package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.ServiceEntryPO;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface ServiceEntryMapper
{
    String C = "id,name,icon_url AS iconUrl,url,sort_order AS sortOrder,status";
    @Select("SELECT " + C + " FROM service_entry WHERE status=1 ORDER BY sort_order,id")
    List<ServiceEntryPO> enabled();
    @Select("SELECT " + C + " FROM service_entry ORDER BY sort_order,id LIMIT #{size} OFFSET #{offset}")
    List<ServiceEntryPO> page(@Param("offset") int offset, @Param("size") int size);
    @Select("SELECT COUNT(*) FROM service_entry")
    int count();
    @Select("SELECT " + C + " FROM service_entry WHERE id=#{id}")
    ServiceEntryPO detail(Long id);
    @Insert("INSERT INTO service_entry(name,icon_url,url,sort_order,status) VALUES(#{name},#{iconUrl},#{url},#{sortOrder},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceEntryPO row);
    @Update("UPDATE service_entry SET name=#{name},icon_url=#{iconUrl},url=#{url},sort_order=#{sortOrder},status=#{status} WHERE id=#{id}")
    int update(ServiceEntryPO row);
    @Delete("DELETE FROM service_entry WHERE id=#{id}")
    int delete(Long id);
}
