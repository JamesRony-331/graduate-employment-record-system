package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.wzbc.EmploymentServer.Service.StudentManagementService;
import com.wzbc.EmploymentServer.entity.DTO.Student.*;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController extends ABaseController
{
    @Autowired
    private StudentManagementService service;

    /** userId 为 sys_user.id，仅允许查询当前登录账号的管理范围。 */
    @GetMapping("/by-user/{userId}")
    public ResponseVO<List<UserMsgVO>> selectByUserId(@PathVariable("userId") Long userId)
    {
        return getSuccessResponseVO(service.selectByUserId(getAccountId(), userId));
    }

    @PostMapping("/add")
    public ResponseVO<UserMsgVO> add(@Valid @RequestBody AddStudentDTO dto)
    {
        return getSuccessResponseVO(service.add(getAccountId(), dto));
    }

    @GetMapping("/{uuid}")
    public ResponseVO<UserMsgVO> detail(@PathVariable("uuid") Long uuid)
    {
        return getSuccessResponseVO(service.detail(getAccountId(), uuid));
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<UserMsgVO>>
    page(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize,
         @RequestParam(required = false) Long collegeId, @RequestParam(required = false) Long classId)
    {
        return getSuccessResponseVO(service.page(getAccountId(), pageNum, pageSize, collegeId, classId));
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateStudentDTO dto)
    {
        service.update(getAccountId(), dto);
        return getSuccessResponseVO("修改学生信息成功");
    }
}
