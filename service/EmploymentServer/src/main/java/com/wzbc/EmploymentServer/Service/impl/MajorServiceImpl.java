package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Service.MajorService;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.entity.DTO.Major.*;
import com.wzbc.EmploymentServer.entity.PO.MajorPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class MajorServiceImpl implements MajorService
{
    private final MajorMapper mapper;
    private final CollegeMapper colleges;
    public MajorServiceImpl(MajorMapper mapper, CollegeMapper colleges)
    {
        this.mapper = mapper;
        this.colleges = colleges;
    }
    private void requireCollege(Long id)
    {
        if (id == null || id <= 0 || colleges.selectById(id) == null)
            throw new BusinessException("所属学院不存在");
    }
    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("专业ID必须为正数");
    }
    private String normalizeName(String name)
    {
        if (name == null || name.isBlank() || name.trim().length() > 100)
            throw new BusinessException("专业名称不能为空且不能超过100字");
        return name.trim();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AddMajorDTO dto)
    {
        requireCollege(dto.getCollegeId());
        MajorPO record = new MajorPO();
        record.setCollegeId(dto.getCollegeId())
        ;
        record.setMajorName(normalizeName(dto.getMajorName()))
        ;
        try
        {
            if (mapper.insert(record) != 1)
                throw new BusinessException("新增专业失败");
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BusinessException("专业信息重复或学院关联无效");
        }
        return record.getId();
    }
    @Override
    public MajorPO detail(Long id)
    {
        checkId(id);
        var record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("专业不存在");
        return record;
    }
    @Override
    public PageResult<MajorPO> page(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        int count = mapper.selectCount();
        return new PageResult<>(mapper.selectByPage((pageNum - 1) * pageSize, pageSize), pageNum, pageSize, count,
                                (int)((count + (long)pageSize - 1) / pageSize));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateMajorDTO dto)
    {
        checkId(dto.getId());
        var record = mapper.selectByIdForUpdate(dto.getId());
        if (record == null)
            throw new BusinessException("专业不存在");
        if (dto.getCollegeId() == null && dto.getMajorName() == null)
            throw new BusinessException("请至少提供一个需要更新的字段");
        if (dto.getCollegeId() != null)
        {
            requireCollege(dto.getCollegeId());
            if (!Objects.equals(dto.getCollegeId(), record.getCollegeId()) && mapper.countReferences(dto.getId()) > 0)
                throw new BusinessException("专业已有班级或学生关联，不能变更所属学院");
            record.setCollegeId(dto.getCollegeId())
            ;
        }
        if (dto.getMajorName() != null)
            record.setMajorName(normalizeName(dto.getMajorName()))
        ;
        try
        {
            mapper.update(record);
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BusinessException("专业信息重复或学院关联无效");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id)
    {
        checkId(id);
        if (mapper.selectByIdForUpdate(id) == null)
            throw new BusinessException("专业不存在");
        if (mapper.countReferences(id) > 0)
            throw new BusinessException("专业已关联班级或学生，不能删除");
        try
        {
            if (mapper.deleteById(id) != 1)
                throw new BusinessException("专业不存在");
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BusinessException("专业存在关联数据，不能删除");
        }
    }
}
