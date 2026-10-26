package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.CounselorClassService;
import com.wzbc.EmploymentServer.entity.DTO.CounselorClass.*;
import com.wzbc.EmploymentServer.entity.PO.CounselorClassPO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import java.util.List;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounselorClassServiceImpl implements CounselorClassService
{
    private final CounselorClassMapper mapper;
    private final CounselorMapper counselors;
    private final ClassMapper classes;

    public CounselorClassServiceImpl(CounselorClassMapper mapper, CounselorMapper counselors, ClassMapper classes)
    {
        this.mapper = mapper;
        this.counselors = counselors;
        this.classes = classes;
    }

    @Override
    @Transactional
    public Long add(AddCounselorClassDTO dto)
    {
        int roleType = dto.getRoleType() == null ? 1 : dto.getRoleType();
        validate(dto.getCounselorId(), dto.getClassId(), roleType, null);
        var record = CopyTools.copy(dto, CounselorClassPO.class);
        record.setRoleType(roleType)
        ;
        if (mapper.insert(record) != 1)
            throw new BusinessException("添加辅导员班级关系失败");
        return record.getId();
    }

    @Override
    public List<ClassPO> selectClassesByCounselorId(Long counselorId)
    {
        if (counselorId == null || counselorId <= 0 || counselors.selectById(counselorId) == null)
        {
            throw new BusinessException("辅导员不存在");
        }
        return mapper.selectClassesByCounselorId(counselorId);
    }

    @Override
    public CounselorClassPO selectById(Long id)
    {
        checkId(id);
        var record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("辅导员班级关系不存在");
        return record;
    }

    @Override
    public PageResult<CounselorClassPO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, mapper.selectCount());
        return new PageResult<>(mapper.selectByPage(query), pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    @Transactional
    public void update(UpdateCounselorClassDTO dto)
    {
        checkId(dto.getId());
        var record = mapper.selectByIdForUpdate(dto.getId());
        if (record == null)
            throw new BusinessException("辅导员班级关系不存在");
        if (dto.getCounselorId() == null && dto.getClassId() == null && dto.getRoleType() == null)
        {
            throw new BusinessException("请至少提供一个需要更新的字段");
        }
        if (dto.getCounselorId() != null)
            record.setCounselorId(dto.getCounselorId())
        ;
        if (dto.getClassId() != null)
            record.setClassId(dto.getClassId())
        ;
        if (dto.getRoleType() != null)
            record.setRoleType(dto.getRoleType())
        ;
        validate(record.getCounselorId(), record.getClassId(), record.getRoleType(), record.getId());
        mapper.update(record);
    }

    @Override
    public void delete(Long id)
    {
        checkId(id);
        if (mapper.deleteById(id) != 1)
            throw new BusinessException("辅导员班级关系不存在");
    }

    private void validate(Long counselorId, Long classId, Integer roleType, Long excludeId)
    {
        // 同一班级的分配操作串行执行，避免重复绑定。
        if (classId == null || classId <= 0 || classes.selectByIdForUpdate(classId) == null)
        {
            throw new BusinessException("班级不存在");
        }
        if (counselorId == null || counselorId <= 0 || counselors.selectById(counselorId) == null)
        {
            throw new BusinessException("辅导员不存在");
        }
        if (roleType == null || (roleType != 1 && roleType != 2))
        {
            throw new BusinessException("辅导员类型只能为1（主辅导员）或2（协管辅导员）");
        }
        if (mapper.countDuplicate(counselorId, classId, excludeId) > 0)
        {
            throw new BusinessException("该辅导员已绑定此班级");
        }
    }

    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("关系ID必须为正数");
    }
}
