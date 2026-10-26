package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.ReviewFilingDTO;

import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.AddEmployDTO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFillingVO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFilingManagementVO;

import java.util.List;

public interface EmploymentFilingService
{
    void updateByCounselor(Long accountId, ReviewFilingDTO dto);
    void updateByDepartment(Long accountId, ReviewFilingDTO dto);

    List<EmploymentFilingManagementVO> getPendingReviewFilings(Long accountId, Long userId);
    List<EmploymentFilingManagementVO> getArchivedFilings(Long accountId, Long userId);

    void addEmploymentFiling(Long UserUuid, AddEmployDTO addEmployDTO);

    EmploymentFillingVO getFilingByStudentUuid(Long userUuid);

    List<EmploymentFillingVO> getFilingsByStudentUuid(Long userUuid);
}
