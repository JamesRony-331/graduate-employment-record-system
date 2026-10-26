package com.wzbc.EmploymentServer.Mapper.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;
import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;

public class WorkStrictHandler extends BaseTypeHandler<WorkComparePO.StrictConditions>
{
    private static final ObjectMapper JSON = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, WorkComparePO.StrictConditions value, JdbcType type)
            throws SQLException
    {
        try
        {
            ps.setString(i, JSON.writeValueAsString(value));
        }
        catch (Exception e)
        {
            throw new SQLException("JSON serialization failed", e);
        }
    }
    private WorkComparePO.StrictConditions read(String value) throws SQLException
    {
        if (value == null)
            return null;
        try
        {
            return JSON.readValue(value, new TypeReference<WorkComparePO.StrictConditions>()
            {
            });
        }
        catch (Exception e)
        {
            throw new SQLException("Invalid stored JSON", e);
        }
    }
    @Override
    public WorkComparePO.StrictConditions getNullableResult(ResultSet rs, String name) throws SQLException
    {
        return read(rs.getString(name));
    }
    @Override
    public WorkComparePO.StrictConditions getNullableResult(ResultSet rs, int i) throws SQLException
    {
        return read(rs.getString(i));
    }
    @Override
    public WorkComparePO.StrictConditions getNullableResult(CallableStatement cs, int i) throws SQLException
    {
        return read(cs.getString(i));
    }
}
