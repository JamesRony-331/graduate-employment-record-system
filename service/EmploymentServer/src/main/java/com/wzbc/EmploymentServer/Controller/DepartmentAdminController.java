package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Service.DepartmentAdminService;
import com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin.*;
import com.wzbc.EmploymentServer.entity.VO.DepartmentAdmin.DepartmentAdminVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/department-admin")
public class DepartmentAdminController extends ABaseController
{
    @Autowired
    private DepartmentAdminService service;

    @PostMapping("/add")
    public ResponseVO<DepartmentAdminVO> add(@Valid @RequestBody AddDepartmentAdminDTO dto)
    {
        return getSuccessResponseVO(service.add(dto));
    }

    @GetMapping("/page")
    public ResponseVO<PageResult<DepartmentAdminVO>> selectByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.selectByPage(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseVO<DepartmentAdminVO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.selectById(id));
    }

    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateDepartmentAdminDTO dto)
    {
        service.update(dto);
        return getSuccessResponseVO("修改院系管理员成功");
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(id);
        return getSuccessResponseVO("删除院系管理员成功");
    }
}
