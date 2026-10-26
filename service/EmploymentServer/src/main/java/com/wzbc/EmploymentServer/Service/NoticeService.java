package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.Notice.*;
import com.wzbc.EmploymentServer.entity.PO.NoticePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface NoticeService
{
    NoticePO publishedDetail(Long id);
    PageResult<NoticePO> publishedPage(int pageNum, int pageSize);
    Long add(Long accountId, AddNoticeDTO dto);
    NoticePO detail(Long id);
    PageResult<NoticePO> page(int pageNum, int pageSize);
    void update(Long accountId, UpdateNoticeDTO dto);
    void delete(Long accountId, Long id);
}
