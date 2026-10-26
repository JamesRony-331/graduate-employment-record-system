package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Service.NoticeService;
import com.wzbc.EmploymentServer.entity.DTO.Notice.*;
import com.wzbc.EmploymentServer.entity.PO.NoticePO;
import com.wzbc.EmploymentServer.entity.VO.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
public class NoticeController extends ABaseController
{
    @Autowired
    private NoticeService service;

    @GetMapping("/published/page")
    public ResponseVO<PageResult<NoticePO>> publishedPage(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.publishedPage(pageNum, pageSize));
    }
    @GetMapping("/published/{id}")
    public ResponseVO<NoticePO> publishedDetail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.publishedDetail(id));
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody AddNoticeDTO dto)
    {
        return getSuccessResponseVO(service.add(getAccountId(), dto));
    }
    @GetMapping("/{id}")
    public ResponseVO<NoticePO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.detail(id));
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<NoticePO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.page(pageNum, pageSize));
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateNoticeDTO dto)
    {
        service.update(getAccountId(), dto);
        return getSuccessResponseVO("修改公告成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(getAccountId(), id);
        return getSuccessResponseVO("删除公告成功");
    }
}
