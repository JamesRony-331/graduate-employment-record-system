package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Service.EmploymentFilingService;
import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.AddEmployDTO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFillingVO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFilingManagementVO;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.validation.Valid;
import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.ReviewFilingDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/employment-filing")
@RestController
public class EmploymentFilingController extends ABaseController
{
    @Autowired
    private EmploymentFilingService employmentFilingService;

    @PutMapping("/review/counselor")
    public ResponseVO<String> reviewByCounselor(@Valid @RequestBody ReviewFilingDTO dto)
    {
        employmentFilingService.updateByCounselor(getAccountId(), dto);
        return getSuccessResponseVO("辅导员审核成功");
    }

    @PutMapping("/review/department")
    public ResponseVO<String> reviewByDepartment(@Valid @RequestBody ReviewFilingDTO dto)
    {
        employmentFilingService.updateByDepartment(getAccountId(), dto);
        return getSuccessResponseVO("院系审核成功");
    }

    /** userId 为当前登录账号的 sys_user.id。 */
    @GetMapping("/pending-review/{userId}")
    public ResponseVO<List<EmploymentFilingManagementVO>> pendingReview(@PathVariable("userId") Long userId)
    {
        return getSuccessResponseVO(employmentFilingService.getPendingReviewFilings(getAccountId(), userId));
    }

    @GetMapping("/archived/{userId}")
    public ResponseVO<List<EmploymentFilingManagementVO>> archived(@PathVariable("userId") Long userId)
    {
        return getSuccessResponseVO(employmentFilingService.getArchivedFilings(getAccountId(), userId));
    }

    @RequestMapping("/AddEmploymentFiling")
    public ResponseVO AddEmploymentFiling(@RequestBody AddEmployDTO addEmployDTO)
    {
        employmentFilingService.addEmploymentFiling(getUserUuid(), addEmployDTO);
        return getSuccessResponseVO("添加成功");
    }

    //查找本人的最新纪录
    @GetMapping("/getLastFiling")
    public ResponseVO getLastFiling()
    {
        EmploymentFillingVO filing = employmentFilingService.getFilingByStudentUuid(getUserUuid());
        return getSuccessResponseVO(filing);
    }

    //查找本人的所有纪录
    @GetMapping("/getFilings")
    public ResponseVO getFilings()
    {
        List<EmploymentFillingVO> filings = employmentFilingService.getFilingsByStudentUuid(getUserUuid());
        return getSuccessResponseVO(filings);
    }
}
