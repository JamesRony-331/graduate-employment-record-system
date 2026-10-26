package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.entity.DTO.Menu.MenuDTO;
import com.wzbc.EmploymentServer.entity.PO.*;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/menu")
public class MenuController extends ABaseController
{
    private final SysMenuMapper mapper;
    private final SysUserRoleMapper roles;
    private final ObjectMapper json;
    public MenuController(SysMenuMapper mapper, SysUserRoleMapper roles, ObjectMapper json)
    {
        this.mapper = mapper;
        this.roles = roles;
        this.json = json;
    }
    private void admin()
    {
        if (roles.selectEnabledRolesByUserId(getAccountId()).stream().noneMatch(r -> "ADMIN".equals(r.getRoleCode())))
            throw new BusinessException(403, "仅系统管理员可管理菜单");
    }
    @GetMapping("/mine")
    public ResponseVO<List<SysMenuPO>> mine(@RequestParam(defaultValue = "ADMIN") String clientType)
    {
        if (!Set.of("ADMIN", "STUDENT").contains(clientType))
            throw new BusinessException("所属端无效");
        return getSuccessResponseVO(mapper.mine(getAccountId(), clientType));
    }
    @GetMapping("/all")
    public ResponseVO<List<SysMenuPO>> all()
    {
        admin();
        return getSuccessResponseVO(mapper.all());
    }
    @GetMapping("/roles")
    public ResponseVO<List<SysRolePO>> roles()
    {
        admin();
        return getSuccessResponseVO(mapper.roles());
    }
    @GetMapping("/role/{id}")
    public ResponseVO<List<Long>> granted(@PathVariable Long id)
    {
        admin();
        return getSuccessResponseVO(mapper.granted(id));
    }
    private SysMenuPO convert(MenuDTO dto, Long id)
    {
        var row = json.convertValue(dto, SysMenuPO.class);
        row.setId(id);
        if (row.getParentId() == null)
            row.setParentId(0L);
        Set<Long> visited = new HashSet<>();
        Long parent = row.getParentId();
        while (parent != 0)
        {
            if (Objects.equals(parent, id) || !visited.add(parent))
                throw new BusinessException("父菜单不能形成循环");
            var p = mapper.detail(parent);
            if (p == null || !p.getClientType().equals(row.getClientType()) || "BUTTON".equals(p.getMenuType()))
                throw new BusinessException("父菜单无效");
            parent = p.getParentId();
        }
        return row;
    }
    @PostMapping
    public ResponseVO<Long> add(@Valid @RequestBody MenuDTO dto)
    {
        admin();
        var row = convert(dto, null);
        mapper.insert(row);
        return getSuccessResponseVO(row.getId());
    }
    @PutMapping("/{id}")
    public ResponseVO<String> update(@PathVariable Long id, @Valid @RequestBody MenuDTO dto)
    {
        admin();
        if (mapper.detail(id) == null)
            throw new BusinessException(404, "菜单不存在");
        mapper.update(convert(dto, id));
        return getSuccessResponseVO("保存成功");
    }
    @DeleteMapping("/{id}")
    public ResponseVO<String> delete(@PathVariable Long id)
    {
        admin();
        if (mapper.children(id) > 0)
            throw new BusinessException("请先删除子菜单");
        if (mapper.delete(id) != 1)
            throw new BusinessException(404, "菜单不存在");
        return getSuccessResponseVO("删除成功");
    }
    @PutMapping("/role/{id}")
    @Transactional
    public ResponseVO<String> grant(@PathVariable Long id, @RequestBody List<Long> ids)
    {
        admin();
        if (ids == null || ids.size() > 1000 || mapper.roles().stream().noneMatch(r -> r.getId().equals(id)))
            throw new BusinessException("角色或授权数据无效");
        for (Long menuId : new HashSet<>(ids))
            if (menuId == null || mapper.detail(menuId) == null)
                throw new BusinessException("菜单不存在");
        mapper.clearRole(id);
        for (Long menuId : new HashSet<>(ids))
            mapper.grant(id, menuId);
        return getSuccessResponseVO("授权成功");
    }
}
