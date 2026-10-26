package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.Counselor.AddCounselorDTO;
import com.wzbc.EmploymentServer.entity.DTO.Counselor.UpdateCounselorDTO;
import com.wzbc.EmploymentServer.entity.PO.CounselorPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.Counselor.CounselorVO;

import java.util.List;

public interface CounselorService
{
    void addCouselor(AddCounselorDTO addCounselorTO);

    List<CounselorVO> PageSelect(PageQuery pageQuery);

    void UpdateCounselot(UpdateCounselorDTO updateCounselorDTO);
}
