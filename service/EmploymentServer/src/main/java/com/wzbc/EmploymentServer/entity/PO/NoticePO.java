package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/** 公告，对应 sys_notice 表。 */
@Data
public class NoticePO
{
    private Long id;
    private String title;
    private String content;
    /** 1系统公告 2就业政策 3备案通知 4其他 */
    private Integer noticeType;
    /** 0草稿 1已发布 2已撤回 */
    private Integer status;
    private Integer isTop;
    private Long publisherId;
    private String publisherName;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
