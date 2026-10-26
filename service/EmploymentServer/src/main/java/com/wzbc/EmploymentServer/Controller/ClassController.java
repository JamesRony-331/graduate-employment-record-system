package com.wzbc.EmploymentServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.wzbc.EmploymentServer.Service.ClassService;
import com.wzbc.EmploymentServer.Service.ManagedClassService;
import java.util.List;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.entity.DTO.Class.UpdateClassDTO;
import com.wzbc.EmploymentServer.entity.DTO.Class.AddClassDTO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import com.wzbc.EmploymentServer.entity.DTO.Class.BindCollegeDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/class")
public class ClassController extends ABaseController
{
    @Autowired
    private ClassService service;

    @Autowired
    private ManagedClassService managedClassService;

    /** 根据当前登录账号查询管理班级，无需客户端传入身份或范围。 */
    @GetMapping("/managed")
    public ResponseVO<List<ClassPO>> selectManagedClasses()
    {
        return getSuccessResponseVO(managedClassService.selectManagedClasses(getAccountId(), null));
    }

    /** 兼容旧路径；辅导员仅能传本人ID，院系管理员按本人学院限定范围。 */
    @GetMapping("/by-counselor/{counselorId}")
    public ResponseVO<List<ClassPO>> selectClassesByCounselorId(@PathVariable("counselorId") Long counselorId)
    {
        return getSuccessResponseVO(managedClassService.selectManagedClasses(getAccountId(), counselorId));
    }

    /** 查询管理范围内指定班级的全部学生，不分页。 */
    @GetMapping("/{classId}/students")
    public ResponseVO<List<UserMsgVO>> selectStudentsByClassId(@PathVariable("classId") Long classId)
    {
        return getSuccessResponseVO(managedClassService.selectStudentsByClassId(getAccountId(), classId));
    }

    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody AddClassDTO dto)
    {
        return getSuccessResponseVO(service.add(dto));
    }

    @GetMapping("/page")
    public ResponseVO<PageResult<ClassPO>> selectByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize)
    {
        return getSuccessResponseVO(service.selectByPage(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseVO<ClassPO> detail(@PathVariable("id") Long id)
    {
        return getSuccessResponseVO(service.selectById(id));
    }

    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody UpdateClassDTO dto)
    {
        service.update(dto);
        return getSuccessResponseVO("修改班级成功");
    }

    @PutMapping("/bind-college")
    public ResponseVO<String> bindCollege(@Valid @RequestBody BindCollegeDTO dto)
    {
        service.bindCollege(dto.getClassId(), dto.getCollegeId());
        return getSuccessResponseVO("班级绑定学院成功");
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable("id") Long id)
    {
        service.delete(id);
        return getSuccessResponseVO("删除班级成功");
    }
}
