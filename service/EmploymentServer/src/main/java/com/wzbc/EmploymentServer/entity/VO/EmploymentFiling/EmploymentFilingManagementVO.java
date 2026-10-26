package com.wzbc.EmploymentServer.entity.VO.EmploymentFiling;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** 审核和归档管理列表，包含记录定位信息及提交资料。 */
@Getter
@Setter
public class EmploymentFilingManagementVO extends EmploymentFillingVO
{
    private Long id;
    private String applicationNo;
    private Long studentUuid;
    private LocalDateTime submittedAt;
    private LocalDateTime archivedAt;
    private Integer version;
    private String destinationType;
    private Long previousFilingId;
    private String changeReason;
    private String destinationDetails;
    private String materials;
}
