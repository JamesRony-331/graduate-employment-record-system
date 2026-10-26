package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Service.NoticeService;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.entity.DTO.Notice.*;
import com.wzbc.EmploymentServer.entity.PO.NoticePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class NoticeServiceImpl implements NoticeService
{
    private final NoticeMapper mapper;
    private final SysUserMapper users;
    public NoticeServiceImpl(NoticeMapper mapper, SysUserMapper users)
    {
        this.mapper = mapper;
        this.users = users;
    }
    @Override
    public NoticePO publishedDetail(Long id)
    {
        checkId(id);
        var record = mapper.selectPublishedById(id);
        if (record == null)
            throw new BusinessException(404, "公告不存在或已撤回");
        return record;
    }
    @Override
    public PageResult<NoticePO> publishedPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        int count = mapper.countPublished();
        return new PageResult<>(mapper.selectPublishedPage((pageNum - 1) * pageSize, pageSize), pageNum, pageSize,
                                count, (int)((count + (long)pageSize - 1) / pageSize));
    }
    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("公告ID必须为正数");
    }
    private void validate(NoticePO r)
    {
        if (r.getTitle() == null || r.getTitle().isBlank() || r.getTitle().length() > 200)
            throw new BusinessException("公告标题不能为空且不超过200字");
        if (r.getContent() == null || r.getContent().isBlank())
            throw new BusinessException("公告正文不能为空");
        if (r.getNoticeType() == null || r.getNoticeType() < 1 || r.getNoticeType() > 4)
            throw new BusinessException("公告类型必须为1至4");
        if (r.getStatus() == null || r.getStatus() < 0 || r.getStatus() > 2)
            throw new BusinessException("公告状态必须为0至2");
        if (r.getIsTop() == null || r.getIsTop() < 0 || r.getIsTop() > 1)
            throw new BusinessException("置顶标记必须为0或1");
    }
    private NoticePO owned(Long accountId, Long id)
    {
        checkId(id);
        var r = mapper.selectByIdForUpdate(id);
        if (r == null)
            throw new BusinessException("公告不存在");
        if (!Objects.equals(r.getPublisherId(), accountId))
            throw new BusinessException(403, "只能修改或删除本人公告");
        return r;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Long accountId, AddNoticeDTO dto)
    {
        var user = users.selectById(accountId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()) || !"STAFF".equals(user.getAccountType()))
            throw new BusinessException(403, "需要有效的教职工账号");
        var record = CopyTools.copy(dto, NoticePO.class);
        validate(record);
        record.setPublisherId(user.getId())
        ;
        record.setPublisherName(user.getRealName())
        ;
        var now = LocalDateTime.now();
        record.setCreatedAt(now)
        ;
        record.setUpdatedAt(now)
        ;
        if (record.getStatus() == 1)
            record.setPublishTime(now)
        ;
        if (mapper.insert(record) != 1)
            throw new BusinessException("新增公告失败");
        return record.getId();
    }
    @Override
    public NoticePO detail(Long id)
    {
        checkId(id);
        var record = mapper.selectById(id);
        if (record == null)
            throw new BusinessException("公告不存在");
        return record;
    }
    @Override
    public PageResult<NoticePO> page(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        int count = mapper.selectCount();
        return new PageResult<>(mapper.selectByPage((pageNum - 1) * pageSize, pageSize), pageNum, pageSize, count,
                                (int)((count + (long)pageSize - 1) / pageSize));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long accountId, UpdateNoticeDTO dto)
    {
        var r = owned(accountId, dto.getId());
        if (dto.getTitle() == null && dto.getContent() == null && dto.getNoticeType() == null &&
            dto.getStatus() == null && dto.getIsTop() == null)
            throw new BusinessException("请至少提供一个需要更新的字段");
        int previous = r.getStatus();
        if (dto.getTitle() != null)
            r.setTitle(dto.getTitle());
        if (dto.getContent() != null)
            r.setContent(dto.getContent());
        if (dto.getNoticeType() != null)
            r.setNoticeType(dto.getNoticeType());
        if (dto.getStatus() != null)
            r.setStatus(dto.getStatus());
        if (dto.getIsTop() != null)
            r.setIsTop(dto.getIsTop());
        validate(r);
        var now = LocalDateTime.now();
        r.setUpdatedAt(now);
        if (r.getStatus() == 1 && previous != 1)
            r.setPublishTime(now);
        if (r.getStatus() == 0)
            r.setPublishTime(null);
        if (mapper.update(r) != 1)
            throw new BusinessException("修改公告失败");
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long accountId, Long id)
    {
        owned(accountId, id);
        if (mapper.deleteById(id) != 1)
            throw new BusinessException("公告不存在");
    }
}
