package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import java.sql.Date;
import java.time.DateTimeException;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.Mapper.ClassMapper;
import org.springframework.transaction.annotation.Transactional;
import com.wzbc.EmploymentServer.Service.StudentService;
import com.wzbc.EmploymentServer.entity.DTO.User.UpdateUserMsgAdmin;
import com.wzbc.EmploymentServer.entity.DTO.User.UserRegDTO;
import com.wzbc.EmploymentServer.entity.DTO.User.UserUpdateMsgDTO;
import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StudentServiceImpl implements StudentService
{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ClassMapper classMapper;

    private ClassPO validateClass(Long classId)
    {
        if (classId == null || classId <= 0)
            throw new BusinessException("请选择有效班级");
        var record = classMapper.selectByIdForUpdate(classId);
        if (record == null)
            throw new BusinessException("所选班级不存在");
        if (record.getCollegeId() == null)
            throw new BusinessException("所选班级尚未设置学院");
        if (record.getMajorId() == null)
            throw new BusinessException("所选班级尚未设置专业");
        return record;
    }

    @Override
    @Transactional
    public void addStudent(UserRegDTO userRegDTO)
    {
        if (userRegDTO == null || userRegDTO.getStudentNo() == null || userRegDTO.getStudentNo().isBlank())
        {
            throw new BusinessException("学号不能为空");
        }
        if (userRegDTO.getIdCard() == null || !userRegDTO.getIdCard().matches("[0-9]{17}[0-9Xx]"))
        {
            throw new BusinessException("请填写18位身份证号");
        }
        validateYears(userRegDTO.getEnrollmentYear(), userRegDTO.getGraduationYear());
        var selectedClass = validateClass(userRegDTO.getClassId());
        if (userRegDTO.getMajorId() != null && !userRegDTO.getMajorId().equals(selectedClass.getMajorId()))
            throw new BusinessException("专业必须与所选班级一致");
        if (userRegDTO.getCollegeId() != null && !userRegDTO.getCollegeId().equals(selectedClass.getCollegeId()))
            throw new BusinessException("学院必须与所选班级一致");
        userRegDTO.setStudentNo(userRegDTO.getStudentNo().trim());
        String stuNo = userRegDTO.getStudentNo();
        checkStudentNoAvailable(stuNo);
        UserPO userPO = CopyTools.copy(userRegDTO, UserPO.class);
        userPO.setMajorId(selectedClass.getMajorId());
        userPO.setCollegeId(selectedClass.getCollegeId());
        userPO.setCreatedAt(LocalDateTime.now());
        userPO.setUpdatedAt(LocalDateTime.now());
        LocalDate date;
        try
        {
            date = LocalDate.parse(userRegDTO.getIdCard().substring(6, 14), DateTimeFormatter.BASIC_ISO_DATE);
        }
        catch (DateTimeException e)
        {
            throw new BusinessException("身份证出生日期不合法");
        }
        userPO.setBirthDate(Date.valueOf(date));
        userPO.setGender(userRegDTO.getIdCard().charAt(16) % 2 == 0 ? 0 : 1);
        userMapper.InsertUser(userPO);
    }

    @Override
    public void updateUserStu(Long uuid, UserUpdateMsgDTO userMsgDTO)
    {
        requireExisting(uuid);
        UserPO userPO = CopyTools.copy(userMsgDTO, UserPO.class);
        userPO.setUuid(uuid);
        saveUserChanges(userPO);
    }

    @Override
    @Transactional
    public void updateUserAdmin(UpdateUserMsgAdmin userMsgDTO)
    {
        var existing = requireExisting(userMsgDTO.getUuid());
        UserPO userPO = CopyTools.copy(userMsgDTO, UserPO.class);
        if (userMsgDTO.getIdCard() != null)
        {
            if (!userMsgDTO.getIdCard().matches("[0-9]{17}[0-9Xx]"))
                throw new BusinessException("请填写18位身份证号");
            try
            {
                userPO.setBirthDate(Date.valueOf(
                        LocalDate.parse(userMsgDTO.getIdCard().substring(6, 14), DateTimeFormatter.BASIC_ISO_DATE)));
            }
            catch (DateTimeException e)
            {
                throw new BusinessException("身份证出生日期不合法");
            }
            userPO.setGender(userMsgDTO.getIdCard().charAt(16) % 2 == 0 ? 0 : 1);
        }
        var enrollment =
                userMsgDTO.getEnrollmentYear() != null ? userMsgDTO.getEnrollmentYear() : existing.getEnrollmentYear();
        var graduation =
                userMsgDTO.getGraduationYear() != null ? userMsgDTO.getGraduationYear() : existing.getGraduationYear();
        validateYears(enrollment, graduation);
        if (userMsgDTO.getClassId() != null || userMsgDTO.getMajorId() != null || userMsgDTO.getCollegeId() != null)
        {
            var selectedClass =
                    validateClass(userMsgDTO.getClassId() != null ? userMsgDTO.getClassId() : existing.getClassId());
            if (userMsgDTO.getMajorId() != null && !userMsgDTO.getMajorId().equals(selectedClass.getMajorId()))
                throw new BusinessException("专业必须与所选班级一致");
            if (userMsgDTO.getCollegeId() != null && !userMsgDTO.getCollegeId().equals(selectedClass.getCollegeId()))
                throw new BusinessException("学院必须与所选班级一致");
            userPO.setMajorId(selectedClass.getMajorId());
            userPO.setCollegeId(selectedClass.getCollegeId());
        }
        saveUserChanges(userPO);
    }
    private void validateYears(Integer enrollment, Integer graduation)
    {
        if ((enrollment != null && (enrollment < 1900 || enrollment > 9999)) ||
            (graduation != null && (graduation < 1900 || graduation > 9999)))
            throw new BusinessException("年份须在1900至9999之间");
        if (enrollment != null && graduation != null && graduation < enrollment)
            throw new BusinessException("毕业年份不能早于入学年份");
    }

    private UserPO requireExisting(Long uuid)
    {
        if (uuid == null)
        {
            throw new BusinessException("uuid不能为空");
        }
        UserPO user = userMapper.selectById(uuid);
        checkStudentExists(user);
        return user;
    }

    private void checkStudentExists(UserPO user)
    {
        if (user == null)
        {
            throw new BusinessException("学生不存在");
        }
    }

    private void checkStudentNoAvailable(String studentNo)
    {
        if (!userMapper.SelectUserByStudentNoOrIdCard(studentNo, null).isEmpty())
        {
            throw new BusinessException("学号已存在");
        }
    }

    private void saveUserChanges(UserPO changes)
    {
        changes.setUpdatedAt(LocalDateTime.now());
        if (userMapper.UpdateUser(changes) != 1)
        {
            throw new BusinessException("学生不存在或信息未更新");
        }
    }
}
