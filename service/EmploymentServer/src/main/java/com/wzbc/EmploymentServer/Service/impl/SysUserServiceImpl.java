package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.entity.VO.User.UserProfileVO;

import java.util.Locale;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Mapper.SysRoleMapper;
import com.wzbc.EmploymentServer.Service.SysUserRoleService;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.Service.StudentService;
import com.wzbc.EmploymentServer.Service.SysUserService;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.entity.VO.SysUser.*;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.util.CopyTools;
import com.wzbc.EmploymentServer.util.StringTools;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

@Service
public class SysUserServiceImpl implements SysUserService
{
    private final SysUserMapper sysUserMapper;
    private final StudentService studentService;
    private final UserMapper userMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleService sysUserRoleService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserServiceImpl(SysUserMapper sysUserMapper, StudentService studentService, UserMapper userMapper,
                              SysRoleMapper sysRoleMapper, SysUserRoleService sysUserRoleService)
    {
        this.sysUserMapper = sysUserMapper;
        this.studentService = studentService;
        this.userMapper = userMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleService = sysUserRoleService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getProfile(Long userId)
    {
        if (userId == null)
            throw new BusinessException(401, "请先登录");
        SysUserPO account = sysUserMapper.selectById(userId);
        if (account == null || !Integer.valueOf(1).equals(account.getStatus()))
        {
            throw new BusinessException(401, "账号不存在或已禁用");
        }
        UserProfileVO profile = new UserProfileVO();
        if ("STUDENT".equals(account.getAccountType()))
        {
            UserPO student = account.getStudentUuid() == null ? null : userMapper.selectById(account.getStudentUuid());
            if (student == null)
                throw new BusinessException("账号未关联有效学生资料");
            profile = CopyTools.copy(student, UserProfileVO.class);
        }
        else
        {
            profile.setName(account.getRealName());
        }
        profile.setUserId(account.getId());
        profile.setRoles(CopyTools.copyList(sysUserRoleService.selectEnabledRolesByUserId(account.getId()),
                                            SysUserMsgVO.RoleVO.class));
        return profile;
    }

    @Override
    public PageResult<SysUserMsgVO> SelectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, sysUserMapper.selectCount());
        var records = sysUserMapper.SelectByPage(query)
                              .stream()
                              .map(dto ->
                              {
                                  SysUserMsgVO vo = CopyTools.copy(dto, SysUserMsgVO.class);
                                  vo.setRoles(CopyTools.copyList(dto.getRoles(), SysUserMsgVO.RoleVO.class));
                                  return vo;
                              })
                              .toList();
        return new PageResult<>(records, pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserMsgVO register(SysUserRegisterDTO dto)
    {
        String username = normalizeUsername(dto.getUsername());
        if (sysUserMapper.selectByUsername(username) != null)
            throw new BusinessException("账号已存在");
        String type = dto.getAccountType();
        if (!"STUDENT".equals(type) && !"STAFF".equals(type))
            throw new BusinessException("账号类型不合法");
        SysUserPO user = new SysUserPO();
        user.setUsername(username);
        user.setPassword(encodePassword(dto.getPassword()));
        user.setPasswordAlgorithm("BCRYPT");
        user.setAccountType(type);
        user.setRealName(dto.getRealName());
        user.setStatus(1);
        try
        {
            if ("STUDENT".equals(type))
            {
                if (dto.getStudent() == null || dto.getCounselorId() != null)
                    throw new BusinessException("学生账号必须提供学生资料，不能绑定辅导员身份");
                if (!username.equals(dto.getStudent().getStudentNo() == null ? null
                                                                             : dto.getStudent().getStudentNo().trim()))
                    throw new BusinessException("学生登录账号必须与学号一致");
                studentService.addStudent(dto.getStudent());
                // 保留现有 StudentService.addStudent(void) 签名；在同一事务内按唯一学号取回ID。
                var students = userMapper.SelectUserByStudentNoOrIdCard(username, null);
                if (students.size() != 1)
                    throw new BusinessException("新增学生关联失败");
                user.setStudentUuid(students.getFirst().getUuid());
                user.setRealName(students.getFirst().getName());
            }
            else
            {
                if (dto.getStudent() != null)
                    throw new BusinessException("教职工账号不能携带学生资料");
                user.setCounselorId(dto.getCounselorId());
            }
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            if (sysUserMapper.insert(user) != 1)
                throw new BusinessException("账号创建失败");
            if ("STUDENT".equals(type))
            {
                var studentRole = sysRoleMapper.selectByCode("STUDENT");
                if (studentRole == null || !Integer.valueOf(1).equals(studentRole.getStatus()))
                {
                    throw new BusinessException("学生角色STUDENT不存在或已禁用，请先配置学生角色");
                }
                sysUserRoleService.bind(user.getId(), studentRole.getId());
            }
        }
        catch (DuplicateKeyException e)
        {
            throw new BusinessException("账号、学号或关联信息已存在");
        }
        return CopyTools.copy(user, SysUserMsgVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO login(SysUserLoginDTO dto)
    {
        SysUserPO user = sysUserMapper.selectByUsername(normalizeUsername(dto.getUsername()));
        checkActive(user);
        if (!user.getAccountType().equals(dto.getAccountType()))
            throw new BusinessException("请使用对应身份的登录入口");
        UserPO student = null;
        if ("STUDENT".equals(user.getAccountType()))
        {
            student = user.getStudentUuid() == null ? null : userMapper.selectById(user.getStudentUuid());
            if (student == null)
                throw new BusinessException("账号未关联有效学生资料");
        }
        else if (!"STAFF".equals(user.getAccountType()))
            throw new BusinessException("账号类型不合法");
        verifyAndUpgradePassword(user, dto.getPassword());
        SysUserLoginVO result = new SysUserLoginVO();
        SysUserLoginMsgVO userMsg = CopyTools.copy(user, SysUserLoginMsgVO.class);
        userMsg.setCounselorName(sysUserMapper.selectCounselorNameByUserId(user.getId()));
        result.setUserMsgVO(userMsg);
        if (student != null)
            result.setStudentMsgVO(CopyTools.copy(student, UserMsgVO.class));
        result.setToken(TokenUserDTO.generateAccountToken(user.getUsername(), user.getId(), user.getAccountType(),
                                                          user.getStudentUuid()));
        return result;
    }

    private void verifyAndUpgradePassword(SysUserPO user, String password)
    {
        if (password == null || password.isBlank())
            throw new BusinessException("账号或密码错误");
        if ("MD5".equals(user.getPasswordAlgorithm()))
        {
            String candidate = StringTools.encodeByMD5(password);
            if (candidate == null || user.getPassword() == null ||
                !MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8),
                                       user.getPassword().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)))
                throw new BusinessException("账号或密码错误");
            if (password.getBytes(StandardCharsets.UTF_8).length > 72)
                throw new BusinessException("旧密码长度不兼容，请联系学校重置密码");
            // CAS 避免并发改密被旧密码升级覆盖。
            if (sysUserMapper.upgradePassword(user.getId(), user.getPassword(), passwordEncoder.encode(password)) != 1)
                throw new BusinessException("账号密码已变化，请重新登录");
        }
        else if (!"BCRYPT".equals(user.getPasswordAlgorithm()) ||
                 !passwordEncoder.matches(password, user.getPassword()))
        {
            throw new BusinessException("账号或密码错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Long id, SysUserUpdateDTO dto)
    {
        requireActive(id);
        if (dto.getUsername() != null)
            throw new BusinessException("登录账号不允许修改");
        if (dto.getCounselorId() != null)
            throw new BusinessException("不能通过个人资料修改身份绑定");
        SysUserPO changes = new SysUserPO();
        changes.setId(id);
        changes.setRealName(dto.getRealName());
        if (dto.getPassword() != null)
            changes.setPassword(encodePassword(dto.getPassword()));
        if (sysUserMapper.update(changes) != 1)
            throw new BusinessException("账号不存在或已禁用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id)
    {
        requireActive(id);
        if (sysUserMapper.softDelete(id) != 1)
            throw new BusinessException("账号不存在或已禁用");
    }
    private void requireActive(Long id)
    {
        if (id == null)
            throw new BusinessException("请先登录");
        checkActive(sysUserMapper.selectById(id));
    }
    private void checkActive(SysUserPO user)
    {
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
            throw new BusinessException("账号或密码错误，或账号已禁用");
    }
    private String normalizeUsername(String username)
    {
        if (username == null || username.isBlank() || username.trim().length() > 50)
            throw new BusinessException("账号不能为空且不能超过50个字符");
        return username.trim();
    }
    private String encodePassword(String password)
    {
        if (password == null || password.isBlank() || password.length() < 6 ||
            password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new BusinessException("密码至少6个字符，UTF-8编码后不能超过72字节");
        return passwordEncoder.encode(password);
    }
}
