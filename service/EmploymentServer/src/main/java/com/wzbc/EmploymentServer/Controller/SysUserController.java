package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.entity.VO.User.UserProfileVO;

import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Service.SysUserService;
import com.wzbc.EmploymentServer.Service.StudentService;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.User.*;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import com.wzbc.EmploymentServer.entity.VO.SysUser.*;
import jakarta.validation.Valid;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import org.springframework.web.bind.annotation.*;

/** 统一账号入口；学生资料仍交给 StudentService 处理。 */
@RestController
@RequestMapping("/sys-user")
public class SysUserController extends ABaseController
{
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private StudentService studentService;

    @GetMapping("/profile")
    public ResponseVO<UserProfileVO> profile()
    {
        return getSuccessResponseVO(sysUserService.getProfile(getAccountId()));
    }

    @GetMapping(
    {"/page", "/SelectByPage"
    }) //给同一个方法配置多个 GET 请求路径
    public ResponseVO<PageResult<SysUserMsgVO>> SelectByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(sysUserService.SelectByPage(pageNum, pageSize));
    }

    @PostMapping("/register")
    public ResponseVO<SysUserMsgVO> register(@Valid @RequestBody SysUserRegisterDTO dto)
    {
        return getSuccessResponseVO(sysUserService.register(dto));
    }
    @PostMapping("/login")
    public ResponseVO<SysUserLoginVO> login(@Valid @RequestBody SysUserLoginDTO dto)
    {
        return getSuccessResponseVO(sysUserService.login(dto));
    }
    @PutMapping("/updateInfo")
    public ResponseVO<String> updateInfo(@Valid @RequestBody SysUserUpdateDTO dto)
    {
        sysUserService.updateInfo(getAccountId(), dto);
        return getSuccessResponseVO("修改成功");
    }
    @DeleteMapping("/delete")
    public ResponseVO<String> softDelete()
    {
        sysUserService.softDelete(getAccountId());
        return getSuccessResponseVO("删除成功");
    }
    @PostMapping("/updateMsg")
    public ResponseVO<String> updateStudent(@Valid @RequestBody UserUpdateMsgDTO dto)
    {
        studentService.updateUserStu(getUserUuid(), dto);
        return getSuccessResponseVO("修改成功");
    }
    @PostMapping("/updateMsgAdmin")
    public ResponseVO<String> updateStudentByStaff(@Valid @RequestBody UpdateUserMsgAdmin dto)
    {
        studentService.updateUserAdmin(dto);
        return getSuccessResponseVO("修改成功");
    }
}
