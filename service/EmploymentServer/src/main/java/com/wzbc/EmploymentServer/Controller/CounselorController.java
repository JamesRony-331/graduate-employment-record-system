package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Service.CounselorService;
import com.wzbc.EmploymentServer.entity.DTO.Counselor.AddCounselorDTO;
import com.wzbc.EmploymentServer.entity.DTO.Counselor.UpdateCounselorDTO;
import com.wzbc.EmploymentServer.entity.PO.CounselorPO;
import com.wzbc.EmploymentServer.entity.VO.Counselor.CounselorVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/counselor")
public class CounselorController extends ABaseController
{
    @Autowired
    private CounselorService counselorService;

    @RequestMapping("/add-counselor")
    public ResponseVO addCounselor(@Valid @RequestBody AddCounselorDTO addCounselorTO)
    {
        counselorService.addCouselor(addCounselorTO);
        return getSuccessResponseVO("添加辅导员成功");
    }

    @PutMapping("/UpdateCounselot")
    public ResponseVO<String> UpdateCounselot(@Valid @RequestBody UpdateCounselorDTO updateCounselorDTO)
    {
        counselorService.UpdateCounselot(updateCounselorDTO);
        return getSuccessResponseVO("修改辅导员成功");
    }

    @RequestMapping("/SelectCounselorByPage")
    public ResponseVO SelectCounselorByPage(@RequestBody PageQuery pageQuery)
    {
        List<CounselorVO> counselorVOs = counselorService.PageSelect(pageQuery);
        return getSuccessResponseVO(counselorVOs);
    }

    /** 复用更新逻辑，将辅导员标记为离职，保留数据库记录。 */
    @DeleteMapping("/DeleteCounselor/{id}")
    public ResponseVO<String> DeleteCounselor(@PathVariable("id") @Positive(message = "辅导员ID必须为正数") Long id)
    {
        UpdateCounselorDTO updateCounselorDTO = new UpdateCounselorDTO();
        updateCounselorDTO.setId(id);
        updateCounselorDTO.setStatus(0);
        counselorService.UpdateCounselot(updateCounselorDTO);
        return getSuccessResponseVO("删除辅导员成功");
    }
}
