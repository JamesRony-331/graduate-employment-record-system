package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.WorkSkillMapper;
import com.wzbc.EmploymentServer.Service.WorkSelectService;
import com.wzbc.EmploymentServer.entity.VO.WorkSelect.WorkRecommendationVO;
import java.util.List;

import com.wzbc.EmploymentServer.Exception.BusinessException;

import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSelectDTO;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 岗位推荐入口，返回按相似度降序排列的岗位。 */
@RestController
@RequestMapping("/work-select")
public class WorkSelectController extends ABaseController
{
    @Autowired
    private WorkSkillMapper skills;

    @Autowired
    private WorkSelectService workSelectService;

    private void validateSkills(WorkSelectDTO dto)
    {
        for (String name : dto.getSkills())
            if (skills.exists(name) == 0)
                throw new BusinessException("请选择技能库中已有的技能：" + name);
    }

    @PostMapping
    public ResponseVO<List<WorkRecommendationVO>> submit(@Valid @RequestBody WorkSelectDTO dto)
    {
        validateSkills(dto);

        return getSuccessResponseVO(workSelectService.getRecommendedJobs(dto));
    }
}
