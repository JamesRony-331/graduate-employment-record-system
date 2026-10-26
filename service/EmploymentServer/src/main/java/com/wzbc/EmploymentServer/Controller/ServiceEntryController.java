package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.ServiceEntryMapper;
import com.wzbc.EmploymentServer.entity.PO.ServiceEntryPO;
import com.wzbc.EmploymentServer.entity.DTO.ServiceEntry.ServiceEntryDTO;
import com.wzbc.EmploymentServer.entity.VO.*;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service-entry")
public class ServiceEntryController extends ABaseController
{
    private final ServiceEntryMapper mapper;
    private final com.wzbc.EmploymentServer.Mapper.SysUserRoleMapper roles;
    public ServiceEntryController(ServiceEntryMapper mapper, com.wzbc.EmploymentServer.Mapper.SysUserRoleMapper roles)
    {
        this.mapper = mapper;
        this.roles = roles;
    }
    private void admin()
    {
        if (roles.selectEnabledRolesByUserId(getAccountId()).stream().noneMatch(r -> "ADMIN".equals(r.getRoleCode())))
            throw new BusinessException(403, "仅最高管理员可管理服务");
    }
    @GetMapping("/enabled")
    public ResponseVO<java.util.List<ServiceEntryPO>> enabled()
    {
        return getSuccessResponseVO(mapper.enabled());
    }
    private String address(String value)
    {
        String text = value.trim();
        if (text.matches("^/(?!/)[A-Za-z0-9/_?#=&.%~-]*$") || text.matches("^https?://[A-Za-z0-9][^\\s\\\\]*$"))
            return text;
        throw new BusinessException("地址须为站内路径或 HTTP/HTTPS 地址");
    }
    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("服务ID必须为正数");
    }
    private ServiceEntryPO record(ServiceEntryDTO dto)
    {
        var row = new ServiceEntryPO();
        row.setName(dto.getName().trim());
        row.setIconUrl(address(dto.getIconUrl()));
        row.setUrl(address(dto.getUrl()));
        row.setSortOrder(dto.getSortOrder());
        row.setStatus(dto.getStatus());
        return row;
    }
    @PostMapping("/add")
    public ResponseVO<Long> add(@Valid @RequestBody ServiceEntryDTO dto)
    {
        admin();
        var row = record(dto);
        mapper.insert(row);
        return getSuccessResponseVO(row.getId());
    }
    @GetMapping("/{id}")
    public ResponseVO<ServiceEntryPO> detail(@PathVariable Long id)
    {
        admin();
        checkId(id);
        var row = mapper.detail(id);
        if (row == null)
            throw new BusinessException(404, "服务不存在");
        return getSuccessResponseVO(row);
    }
    @GetMapping("/page")
    public ResponseVO<PageResult<ServiceEntryPO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize)
    {
        admin();
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效");
        int count = mapper.count();
        return getSuccessResponseVO(new PageResult<>(mapper.page((pageNum - 1) * pageSize, pageSize), pageNum, pageSize,
                                                     count, (int)((count + (long)pageSize - 1) / pageSize)));
    }
    @PutMapping("/update")
    public ResponseVO<String> update(@Valid @RequestBody ServiceEntryDTO dto)
    {
        admin();
        checkId(dto.getId());
        var row = record(dto);
        row.setId(dto.getId());
        if (mapper.update(row) == 0 && mapper.detail(row.getId()) == null)
            throw new BusinessException(404, "服务不存在");
        return getSuccessResponseVO("修改成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable Long id)
    {
        admin();
        checkId(id);
        if (mapper.delete(id) != 1)
            throw new BusinessException(404, "服务不存在");
        return getSuccessResponseVO("删除成功");
    }
}
