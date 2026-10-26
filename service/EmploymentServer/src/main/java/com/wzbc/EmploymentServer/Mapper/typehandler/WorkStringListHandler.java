package com.wzbc.EmploymentServer.Mapper.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;
import java.util.List;

public class WorkStringListHandler extends BaseTypeHandler<List<String>>
{
    private static final ObjectMapper JSON = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> value, JdbcType type) throws SQLException
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
    private List<String> read(String value) throws SQLException
    {
        if (value == null)
            return null;
        try
        {
            return JSON.readValue(value, new TypeReference<List<String>>()
            {
            });
        }
        catch (Exception e)
        {
            throw new SQLException("Invalid stored JSON", e);
        }
    }
    @Override
    public List<String> getNullableResult(ResultSet rs, String name) throws SQLException
    {
        return read(rs.getString(name));
    }
    @Override
    public List<String> getNullableResult(ResultSet rs, int i) throws SQLException
    {
        return read(rs.getString(i));
    }
    @Override
    public List<String> getNullableResult(CallableStatement cs, int i) throws SQLException
    {
        return read(cs.getString(i));
    }
}
