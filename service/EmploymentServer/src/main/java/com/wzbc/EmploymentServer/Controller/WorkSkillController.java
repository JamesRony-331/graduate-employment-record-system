package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.WorkSkillMapper;
import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSkillDTO;
import com.wzbc.EmploymentServer.entity.PO.WorkSkillPO;
import com.wzbc.EmploymentServer.entity.VO.*;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/work-skill")
public class WorkSkillController extends ABaseController
{
    private final WorkSkillMapper mapper;
    public WorkSkillController(WorkSkillMapper mapper)
    {
        this.mapper = mapper;
    }
    @GetMapping("/options")
    public ResponseVO<List<WorkSkillPO>> options(@RequestParam(defaultValue = "") String keyword)
    {
        return getSuccessResponseVO(mapper.page(keyword.trim(), 0, 20));
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<WorkSkillPO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(defaultValue = "") String keyword)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效");
        int count = mapper.count(keyword.trim());
        return getSuccessResponseVO(new PageResult<>(mapper.page(keyword.trim(), (pageNum - 1) * pageSize, pageSize),
                                                     pageNum, pageSize, count,
                                                     (int)((count + (long)pageSize - 1) / pageSize)));
    }
    private WorkSkillPO require(Long id)
    {
        if (id == null || id < 1)
            throw new BusinessException("技能ID无效");
        var row = mapper.detail(id);
        if (row == null)
            throw new BusinessException(404, "技能不存在");
        return row;
    }
    @GetMapping("/{id}")
    public ResponseVO<WorkSkillPO> detail(@PathVariable Long id)
    {
        return getSuccessResponseVO(require(id));
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody WorkSkillDTO dto)
    {
        var row = new WorkSkillPO();
        row.setName(dto.getName().trim());
        mapper.insert(row);
        return getSuccessResponseVO(row.getId());
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody WorkSkillDTO dto)
    {
        var row = require(dto.getId());
        String name = dto.getName().trim();
        if (!row.getName().equals(name) && mapper.references(row.getName()) > 0)
            throw new BusinessException("技能已被工作引用，不能改名");
        row.setName(name);
        mapper.update(row);
        return getSuccessResponseVO("修改成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable Long id)
    {
        var row = require(id);
        if (mapper.references(row.getName()) > 0)
            throw new BusinessException("技能已被工作引用，不能删除");
        mapper.delete(id);
        return getSuccessResponseVO("删除成功");
    }
}
