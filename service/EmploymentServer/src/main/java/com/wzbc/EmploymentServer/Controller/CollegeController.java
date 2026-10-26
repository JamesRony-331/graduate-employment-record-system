package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Service.CollegeService;
import com.wzbc.EmploymentServer.entity.DTO.College.UpdateCollegeDTO;
import com.wzbc.EmploymentServer.entity.DTO.College.AddCollegeDTO;
import com.wzbc.EmploymentServer.entity.PO.CollegePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/college")
public class CollegeController extends ABaseController
{
    @Autowired
    private CollegeService service;

    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody AddCollegeDTO dto)
    {
        return getSuccessResponseVO(service.add(dto));
    }

    @GetMapping("/page")
    public ResponseVO<PageResult<CollegePO>> selectByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.selectByPage(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseVO<CollegePO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.selectById(id));
    }

    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateCollegeDTO dto)
    {
        service.update(dto);
        return getSuccessResponseVO("修改学院成功");
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(id);
        return getSuccessResponseVO("删除学院成功");
    }
}
