package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.WorkSkillMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Mapper.WorkCompareMapper;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSelectDTO;
import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkCompareDTO;
import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** 对比记录管理；请求使用 DTO，持久化及查询使用 PO。 */
@RestController
@RequestMapping("/work-compare")
public class WorkCompareController extends ABaseController
{
    @Autowired
    private WorkSkillMapper skills;
    private void validateSkills(WorkSelectDTO dto)
    {
        for (String name : dto.getSkills())
            if (skills.exists(name) == 0)
                throw new BusinessException("请选择技能库中已有的技能：" + name);
    }

    private final WorkCompareMapper mapper;
    private final ObjectMapper json;
    public WorkCompareController(WorkCompareMapper mapper, ObjectMapper json)
    {
        this.mapper = mapper;
        this.json = json;
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody WorkCompareDTO dto)
    {
        validateSkills(dto);
        WorkComparePO record = json.convertValue(dto, WorkComparePO.class);
        mapper.insert(record);
        return getSuccessResponseVO(record.getId());
    }
    @GetMapping("/{id}")
    public ResponseVO<WorkComparePO> detail(@PathVariable Long id)
    {
        checkId(id);
        WorkComparePO record = mapper.detail(id);
        if (record == null)
            throw new BusinessException(404, "对比记录不存在");
        return getSuccessResponseVO(record);
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<WorkComparePO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        int count = mapper.count();
        return getSuccessResponseVO(new PageResult<>(mapper.page((pageNum - 1) * pageSize, pageSize), pageNum, pageSize,
                                                     count, (int)((count + (long)pageSize - 1) / pageSize)));
    }
    /** 全量更新；清空的可选字段会覆盖为 null。 */
    @PutMapping("/{id}")
    public ResponseVO<String> update(@PathVariable Long id, @Valid @RequestBody WorkCompareDTO dto)
    {
        checkId(id);
        validateSkills(dto);
        WorkComparePO record = json.convertValue(dto, WorkComparePO.class);
        record.setId(id)
        ;
        if (mapper.update(record) == 0 && mapper.detail(id) == null)
            throw new BusinessException(404, "对比记录不存在");
        return getSuccessResponseVO("修改成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable Long id)
    {
        checkId(id);
        if (mapper.delete(id) != 1)
            throw new BusinessException(404, "对比记录不存在");
        return getSuccessResponseVO("删除成功");
    }
    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("记录ID必须为正数");
    }
}
