package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSelectDTO;
import com.wzbc.EmploymentServer.entity.VO.WorkSelect.WorkRecommendationVO;

import java.util.List;

public interface WorkSelectService
{
    List<WorkRecommendationVO> getRecommendedJobs(WorkSelectDTO dto);
}
