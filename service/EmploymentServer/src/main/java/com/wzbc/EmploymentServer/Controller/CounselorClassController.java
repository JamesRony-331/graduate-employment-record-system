package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Service.CounselorClassService;
import com.wzbc.EmploymentServer.entity.DTO.CounselorClass.*;
import com.wzbc.EmploymentServer.entity.PO.CounselorClassPO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import java.util.List;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counselor-class")
public class CounselorClassController extends ABaseController
{
    @Autowired
    private CounselorClassService service;

    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody AddCounselorClassDTO dto)
    {
        return getSuccessResponseVO(service.add(dto));
    }

    /** counselorId 为辅导员资料ID，不是sys_user账号ID。 */
    @GetMapping("/by-counselor/{counselorId}")
    public ResponseVO<List<ClassPO>> selectClassesByCounselorId(@PathVariable("counselorId") Long counselorId)
    {
        return getSuccessResponseVO(service.selectClassesByCounselorId(counselorId));
    }

    @GetMapping("/page")
    public ResponseVO<PageResult<CounselorClassPO>> selectByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.selectByPage(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseVO<CounselorClassPO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.selectById(id));
    }

    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateCounselorClassDTO dto)
    {
        service.update(dto);
        return getSuccessResponseVO("修改辅导员班级关系成功");
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(id);
        return getSuccessResponseVO("删除辅导员班级关系成功");
    }
}
