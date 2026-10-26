package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.BasicKnowledgeMapper;
import com.wzbc.EmploymentServer.entity.PO.BasicKnowledgePO;
import com.wzbc.EmploymentServer.entity.DTO.BasicKnowledge.BasicKnowledgeDTO;
import com.wzbc.EmploymentServer.entity.VO.*;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic-knowledge")
public class BasicKnowledgeController extends ABaseController
{
    private final BasicKnowledgeMapper mapper;
    public BasicKnowledgeController(BasicKnowledgeMapper mapper)
    {
        this.mapper = mapper;
    }
    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("知识ID必须为正数");
    }
    private BasicKnowledgePO record(BasicKnowledgeDTO dto)
    {
        var row = new BasicKnowledgePO();
        row.setTitle(dto.getTitle().trim());
        row.setAnswer(dto.getAnswer().trim());
        row.setStatus(dto.getStatus());
        return row;
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody BasicKnowledgeDTO dto)
    {
        var row = record(dto);
        mapper.insert(row);
        return getSuccessResponseVO(row.getId());
    }
    @GetMapping("/{id}")
    public ResponseVO<BasicKnowledgePO> detail(@PathVariable Long id)
    {
        checkId(id);
        var row = mapper.detail(id);
        if (row == null)
            throw new BusinessException(404, "知识不存在");
        return getSuccessResponseVO(row);
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<BasicKnowledgePO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效");
        int count = mapper.count();
        return getSuccessResponseVO(new PageResult<>(mapper.page((pageNum - 1) * pageSize, pageSize), pageNum, pageSize,
                                                     count, (int)((count + (long)pageSize - 1) / pageSize)));
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody BasicKnowledgeDTO dto)
    {
        checkId(dto.getId());
        var row = record(dto);
        row.setId(dto.getId());
        if (mapper.update(row) == 0 && mapper.detail(row.getId()) == null)
            throw new BusinessException(404, "知识不存在");
        return getSuccessResponseVO("修改成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable Long id)
    {
        checkId(id);
        if (mapper.delete(id) != 1)
            throw new BusinessException(404, "知识不存在");
        return getSuccessResponseVO("删除成功");
    }
}
