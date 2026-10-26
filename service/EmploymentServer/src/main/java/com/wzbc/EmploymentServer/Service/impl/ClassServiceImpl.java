package com.wzbc.EmploymentServer.Service.impl;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.ClassService;
import com.wzbc.EmploymentServer.entity.DTO.Class.UpdateClassDTO;
import com.wzbc.EmploymentServer.entity.DTO.Class.AddClassDTO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassServiceImpl implements ClassService
{
    private final ClassMapper mapper;
    private final CollegeMapper collegeMapper;
    @Autowired
    private MajorMapper majorMapper;

    private void validateMajor(Long majorId, Long collegeId)
    {
        var major = majorId == null ? null : majorMapper.selectById(majorId);
        if (major == null)
            throw new BusinessException("请选择有效专业");
        if (!Objects.equals(major.getCollegeId(), collegeId))
            throw new BusinessException("专业不属于所选学院，请重新选择专业");
    }

    public ClassServiceImpl(ClassMapper mapper, CollegeMapper collegeMapper)
    {
        this.mapper = mapper;
        this.collegeMapper = collegeMapper;
    }

    @Override
    @Transactional
    public Long add(AddClassDTO dto)
    {
        if (dto.getCollegeId() == null || collegeMapper.selectById(dto.getCollegeId()) == null)
        {
            throw new BusinessException("所属学院不存在");
        }
        validateMajor(dto.getMajorId(), dto.getCollegeId());
        ClassPO record = CopyTools.copy(dto, ClassPO.class);
        if (record.getStudentCount() == null)
            record.setStudentCount(0)
        ;
        mapper.insert(record);
        return record.getId();
    }

    @Override
    public PageResult<ClassPO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, mapper.selectCount());
        return new PageResult<>(mapper.selectByPage(query), pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    public ClassPO selectById(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("班级ID必须为正数");
        ClassPO record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("班级不存在");
        return record;
    }

    @Override
    @Transactional
    public void update(UpdateClassDTO dto)
    {
        ClassPO existing = mapper.selectByIdForUpdate(dto.getId());
        if (existing == null)
            throw new BusinessException("班级不存在");
        if (dto.getClassCode() == null && dto.getClassName() == null && dto.getCollegeId() == null &&
            dto.getMajorId() == null && dto.getEnrollmentYear() == null && dto.getStudentCount() == null)
        {
            throw new BusinessException("请至少提供一个需要更新的字段");
        }
        if (dto.getCollegeId() != null && collegeMapper.selectById(dto.getCollegeId()) == null)
        {
            throw new BusinessException("所属学院不存在");
        }
        validateMajor(dto.getMajorId() != null ? dto.getMajorId() : existing.getMajorId(),
                      dto.getCollegeId() != null ? dto.getCollegeId() : existing.getCollegeId());
        if (mapper.update(CopyTools.copy(dto, ClassPO.class)) == 0)
        {
            selectById(dto.getId());
        }
        if (dto.getMajorId() != null || dto.getCollegeId() != null)
            mapper.syncStudentMajor(dto.getId(), dto.getMajorId() != null ? dto.getMajorId() : existing.getMajorId());
    }

    @Override
    @Transactional
    public void bindCollege(Long classId, Long collegeId)
    {
        if (collegeId == null || collegeId <= 0)
        {
            throw new BusinessException("学院ID必须为正数");
        }
        UpdateClassDTO dto = new UpdateClassDTO();
        dto.setId(classId);
        dto.setCollegeId(collegeId);
        update(dto);
    }

    @Override
    @Transactional
    public void delete(Long id)
    {
        selectById(id);
        if (mapper.countReferences(id) > 0)
        {
            throw new BusinessException("班级仍有关联数据，不能删除");
        }
        try
        {
            if (mapper.deleteById(id) == 0)
                throw new BusinessException("班级不存在");
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BusinessException("班级仍有关联数据，不能删除");
        }
    }
}
