package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.wzbc.EmploymentServer.Mapper.MajorMapper;
import com.wzbc.EmploymentServer.entity.PO.MajorPO;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.wzbc.EmploymentServer.Service.MajorService;
import com.wzbc.EmploymentServer.entity.DTO.Major.*;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/major")
public class MajorController extends ABaseController
{
    private final MajorMapper mapper;
    @Autowired
    private MajorService service;
    public MajorController(MajorMapper mapper)
    {
        this.mapper = mapper;
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody AddMajorDTO dto)
    {
        return getSuccessResponseVO(service.add(dto));
    }
    @GetMapping("/{id}")
    public ResponseVO<MajorPO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.detail(id));
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<MajorPO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.page(pageNum, pageSize));
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateMajorDTO dto)
    {
        service.update(dto);
        return getSuccessResponseVO("修改专业成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(id);
        return getSuccessResponseVO("删除专业成功");
    }
    @GetMapping("/options")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseVO<List<MajorPO>> options()
    {
        return getSuccessResponseVO(mapper.selectAll());
    }
}
