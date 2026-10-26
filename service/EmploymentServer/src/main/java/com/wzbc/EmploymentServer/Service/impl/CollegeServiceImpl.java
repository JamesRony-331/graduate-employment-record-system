package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.CollegeService;
import com.wzbc.EmploymentServer.entity.DTO.College.UpdateCollegeDTO;
import com.wzbc.EmploymentServer.entity.DTO.College.AddCollegeDTO;
import com.wzbc.EmploymentServer.entity.PO.CollegePO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollegeServiceImpl implements CollegeService
{
    private final CollegeMapper mapper;

    public CollegeServiceImpl(CollegeMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Long add(AddCollegeDTO dto)
    {
        CollegePO record = CopyTools.copy(dto, CollegePO.class);
        mapper.insert(record);
        return record.getId();
    }

    @Override
    public PageResult<CollegePO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, mapper.selectCount());
        return new PageResult<>(mapper.selectByPage(query), pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    public CollegePO selectById(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("学院ID必须为正数");
        CollegePO record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("学院不存在");
        return record;
    }

    @Override
    @Transactional
    public void update(UpdateCollegeDTO dto)
    {
        selectById(dto.getId());
        if (dto.getCollegeCode() == null && dto.getCollegeName() == null && dto.getDeanName() == null &&
            dto.getPhone() == null && dto.getDescription() == null)
        {
            throw new BusinessException("请至少提供一个需要更新的字段");
        }
        if (mapper.update(CopyTools.copy(dto, CollegePO.class)) == 0)
        {
            selectById(dto.getId());
        }
    }

    @Override
    @Transactional
    public void delete(Long id)
    {
        selectById(id);
        if (mapper.countReferences(id) > 0)
        {
            throw new BusinessException("学院下仍有绑定的班级或其他关联数据，不能删除");
        }
        try
        {
            if (mapper.deleteById(id) == 0)
                throw new BusinessException("学院不存在");
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BusinessException("学院下仍有绑定的班级或其他关联数据，不能删除");
        }
    }
}
