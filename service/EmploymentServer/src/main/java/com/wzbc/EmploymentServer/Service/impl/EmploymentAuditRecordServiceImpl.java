package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Service.EmploymentAuditRecordService;
import com.wzbc.EmploymentServer.Mapper.EmploymentAuditRecordMapper;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.entity.PO.EmploymentAuditRecordPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EmploymentAuditRecordServiceImpl implements EmploymentAuditRecordService
{
    private final EmploymentAuditRecordMapper mapper;
    public EmploymentAuditRecordServiceImpl(EmploymentAuditRecordMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(EmploymentAuditRecordPO input)
    {
        validate(input);
        var record = CopyTools.copy(input, EmploymentAuditRecordPO.class);
        record.setId(null)
        ;
        record.setAuditedAt(LocalDateTime.now())
        ;
        try
        {
            if (mapper.insert(record) != 1)
                throw new BusinessException("新增审核记录失败");
        }
        catch (DuplicateKeyException e)
        {
            throw new BusinessException("该备案当前阶段已有审核记录");
        }
        return record.getId();
    }

    @Override
    public EmploymentAuditRecordPO selectById(Long id)
    {
        checkId(id);
        var record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("审核记录不存在");
        return record;
    }

    @Override
    public List<EmploymentAuditRecordPO> selectByFilingId(Long filingId)
    {
        checkId(filingId);
        return mapper.selectByFilingId(filingId);
    }

    @Override
    public PageResult<EmploymentAuditRecordPO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        int count = mapper.selectCount();
        return new PageResult<>(mapper.selectByPage((pageNum - 1) * pageSize, pageSize), pageNum, pageSize, count,
                                (int)((count + (long)pageSize - 1) / pageSize));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EmploymentAuditRecordPO record)
    {
        validate(record);
        var existing = selectById(record.getId());
        if (!Objects.equals(existing.getFilingId(), record.getFilingId()) ||
            !Objects.equals(existing.getAuditStage(), record.getAuditStage()))
            throw new BusinessException("不能变更审核记录所属备案或审核阶段");
        if (mapper.update(record) != 1)
            throw new BusinessException("审核记录不存在或更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id)
    {
        checkId(id);
        if (mapper.deleteById(id) != 1)
            throw new BusinessException("审核记录不存在");
    }

    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("ID必须为正数");
    }

    private void validate(EmploymentAuditRecordPO record)
    {
        if (record == null)
            throw new BusinessException("审核记录不能为空");
        checkId(record.getFilingId());
        checkId(record.getAuditorId());
        if (mapper.countFiling(record.getFilingId()) == 0)
            throw new BusinessException("备案不存在");
        if (record.getAuditorName() == null || record.getAuditorName().isBlank() ||
            record.getAuditorName().length() > 64)
            throw new BusinessException("审核人姓名不能为空且不能超过64字");
        if (record.getAuditOpinion() != null && record.getAuditOpinion().length() > 1000)
            throw new BusinessException("审核意见不能超过1000字");
        Integer stage = record.getAuditStage(), role = record.getAuditorRole(), result = record.getAuditResult();
        Integer from = record.getFromStatus(), to = record.getToStatus();
        boolean initial = Objects.equals(stage, 1) && Objects.equals(from, 10);
        boolean review = Objects.equals(stage, 2) && Objects.equals(from, 20);
        boolean pass = Objects.equals(result, 1) && ((initial && Objects.equals(role, 1) && Objects.equals(to, 20)) ||
                                                     (review && Objects.equals(role, 2) && Objects.equals(to, 30)));
        // 与现有建表约束一致：打回由辅导员执行。
        boolean reject =
                Objects.equals(result, 2) && Objects.equals(role, 1) && Objects.equals(to, 40) && (initial || review);
        if (!pass && !reject)
            throw new BusinessException("审核阶段、角色、结果与状态流转不匹配");
        if (reject && (record.getAuditOpinion() == null || record.getAuditOpinion().isBlank()))
            throw new BusinessException("打回时审核意见必填");
    }
}
