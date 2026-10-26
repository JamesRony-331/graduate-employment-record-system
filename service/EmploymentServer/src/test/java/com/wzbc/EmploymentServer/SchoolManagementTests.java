package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Mapper.MajorMapper;
import com.wzbc.EmploymentServer.entity.DTO.Class.AddClassDTO;
import com.wzbc.EmploymentServer.entity.DTO.College.AddCollegeDTO;
import org.springframework.test.util.ReflectionTestUtils;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.ClassMapper;
import com.wzbc.EmploymentServer.Mapper.CollegeMapper;
import com.wzbc.EmploymentServer.Service.impl.ClassServiceImpl;
import com.wzbc.EmploymentServer.Service.impl.CollegeServiceImpl;
import com.wzbc.EmploymentServer.entity.DTO.Class.UpdateClassDTO;
import com.wzbc.EmploymentServer.entity.DTO.College.UpdateCollegeDTO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchoolManagementTests
{
    private SqlSession session;
    private CollegeServiceImpl colleges;
    private ClassServiceImpl classes;

    @BeforeEach
    void setup() throws Exception
    {
        var dataSource =
                new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL", "sa", "");
        var configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        for (String name : new String[]
        {"College", "Class"
        })
        {
            String resource = "mapper/" + name + "Mapper.xml";
            try (var input = getClass().getClassLoader().getResourceAsStream(resource))
            {
                new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        configuration.addMapper(MajorMapper.class);
        session = new SqlSessionFactoryBuilder().build(configuration).openSession(true);
        sql("CREATE TABLE college(id BIGINT AUTO_INCREMENT PRIMARY KEY, college_code VARCHAR(20) NOT NULL UNIQUE, college_name VARCHAR(100) NOT NULL, dean_name VARCHAR(50), phone VARCHAR(20), description VARCHAR(500), created_at TIMESTAMP, updated_at TIMESTAMP)");
        sql("CREATE TABLE `class_info`(id BIGINT AUTO_INCREMENT PRIMARY KEY, class_code VARCHAR(30) NOT NULL UNIQUE, class_name VARCHAR(100) NOT NULL, college_id BIGINT NOT NULL REFERENCES college(id), major_id BIGINT, enrollment_year SMALLINT, student_count INT NOT NULL, created_at TIMESTAMP, updated_at TIMESTAMP)");
        sql("CREATE TABLE college_admin(id BIGINT PRIMARY KEY, college_id BIGINT)");
        sql("CREATE TABLE counselor_class(id BIGINT PRIMARY KEY, class_id BIGINT)");
        sql("CREATE TABLE student(uuid BIGINT PRIMARY KEY, class_id BIGINT, college_id BIGINT, major_id BIGINT, updated_at TIMESTAMP)");
        sql("CREATE TABLE employment_filing(id BIGINT PRIMARY KEY, class_id BIGINT)");
        sql("INSERT INTO college(id,college_code,college_name) VALUES(1,'C1','学院一'),(2,'C2','学院二'),(3,'C3','学院三')");
        sql("INSERT INTO `class_info`(id,class_code,class_name,college_id,student_count) VALUES(1,'B1','班级一',1,10),(2,'B2','班级二',1,20),(3,'B3','班级三',2,30)");
        sql("CREATE TABLE major(id BIGINT AUTO_INCREMENT PRIMARY KEY, college_id BIGINT, major_name VARCHAR(100))");
        sql("INSERT INTO major(id,college_id,major_name) VALUES(1,1,'专业一'),(2,2,'专业二'),(3,3,'专业三')");
        sql("UPDATE class_info SET major_id = college_id");
        var collegeMapper = session.getMapper(CollegeMapper.class);
        colleges = new CollegeServiceImpl(collegeMapper);
        classes = new ClassServiceImpl(session.getMapper(ClassMapper.class), collegeMapper);
        ReflectionTestUtils.setField(classes, "majorMapper", session.getMapper(MajorMapper.class));
    }

    @AfterEach
    void close()
    {
        if (session != null)
            session.close();
    }

    private void sql(String sql) throws Exception
    {
        try (var statement = session.getConnection().createStatement())
        {
            statement.execute(sql);
        }
    }

    @Test
    void creationReturnsIdsAndProtectsCollegeWithBoundClass() throws Exception
    {
        var college = new AddCollegeDTO();
        college.setCollegeCode("NEW");
        college.setCollegeName("新学院");
        Long collegeId = colleges.add(college);
        assertNotNull(collegeId);
        assertEquals("新学院", colleges.selectById(collegeId).getCollegeName());
        assertNotNull(colleges.selectById(collegeId).getCreatedAt());
        var classroom = new AddClassDTO();
        classroom.setClassCode("NEWCLASS");
        classroom.setClassName("新班级");
        classroom.setCollegeId(collegeId);
        sql("INSERT INTO major(id,college_id,major_name) VALUES(4," + collegeId + ",'新专业')");
        classroom.setMajorId(4L);
        Long classId = classes.add(classroom);
        assertNotNull(classId);
        assertEquals(collegeId, classes.selectById(classId).getCollegeId());
        assertEquals(0, classes.selectById(classId).getStudentCount());
        assertThrows(BusinessException.class, () -> colleges.delete(collegeId));
        assertNotNull(colleges.selectById(collegeId));
        classes.delete(classId);
        colleges.delete(collegeId);
        assertThrows(BusinessException.class, () -> colleges.selectById(collegeId));
        classroom.setCollegeId(999L);
        assertThrows(BusinessException.class, () -> classes.add(classroom));
    }

    @Test
    void paginationReturnsCorrectOffsetAndTotals()
    {
        var collegePage = colleges.selectByPage(2, 2);
        assertEquals(3, collegePage.totalCount());
        assertEquals(2, collegePage.total());
        assertEquals(1, collegePage.records().size());
        assertEquals(1L, collegePage.records().getFirst().getId());
        var classPage = classes.selectByPage(2, 2);
        assertEquals(3, classPage.totalCount());
        assertEquals(1L, classPage.records().getFirst().getId());
        assertTrue(classes.selectByPage(3, 2).records().isEmpty());
        assertThrows(BusinessException.class, () -> colleges.selectByPage(0, 10));
        assertThrows(BusinessException.class, () -> classes.selectByPage(1, 0));
    }

    @Test
    void partialUpdatesPreserveOtherFieldsAndCheckCollege()
    {
        var collegeDTO = new UpdateCollegeDTO();
        collegeDTO.setId(1L);
        collegeDTO.setDeanName("王老师");
        colleges.update(collegeDTO);
        assertEquals("王老师", colleges.selectById(1L).getDeanName());
        assertEquals("学院一", colleges.selectById(1L).getCollegeName());
        assertNotNull(colleges.selectById(1L).getUpdatedAt());
        var classDTO = new UpdateClassDTO();
        classDTO.setId(1L);
        classDTO.setStudentCount(0);
        classes.update(classDTO);
        assertEquals(0, classes.selectById(1L).getStudentCount());
        assertEquals("班级一", classes.selectById(1L).getClassName());
        classDTO.setCollegeId(999L);
        assertThrows(BusinessException.class, () -> classes.update(classDTO));
        assertEquals(1L, classes.selectById(1L).getCollegeId());
    }

    @Test
    void deletionRejectsReferencesAndRemovesUnreferencedRecords() throws Exception
    {
        assertThrows(BusinessException.class, () -> colleges.delete(1L));
        sql("INSERT INTO student(uuid,class_id) VALUES(1,1)");
        assertThrows(BusinessException.class, () -> classes.delete(1L));
        sql("INSERT INTO employment_filing VALUES(1,2)");
        assertThrows(BusinessException.class, () -> classes.delete(2L));
        classes.delete(3L);
        assertThrows(BusinessException.class, () -> classes.selectById(3L));
        colleges.delete(2L);
        assertThrows(BusinessException.class, () -> colleges.selectById(2L));
    }

    @Test
    void bindingOverwritesCollegeAndPreservesClassFields()
    {
        assertThrows(BusinessException.class, () -> classes.bindCollege(3L, 3L));
        var update = new UpdateClassDTO();
        update.setId(3L);
        update.setCollegeId(3L);
        update.setMajorId(3L);
        classes.update(update);
        assertEquals(3L, classes.selectById(3L).getCollegeId());
        assertEquals("专业三", classes.selectById(3L).getMajorName());
        assertThrows(BusinessException.class, () -> classes.bindCollege(3L, 999L));
    }

    @Test
    void changingClassMajorSynchronizesStudents() throws Exception
    {
        sql("INSERT INTO major VALUES(4,1,'新专业')");
        sql("INSERT INTO student(uuid,class_id,major_id) VALUES(1,1,1)");
        var update = new UpdateClassDTO();
        update.setId(1L);
        update.setMajorId(4L);
        classes.update(update);
        try (var statement = session.getConnection().createStatement();
             var rs = statement.executeQuery("SELECT major_id FROM student WHERE uuid=1"))
        {
            assertTrue(rs.next());
            assertEquals(4L, rs.getLong(1));
        }
        update.setMajorId(2L);
        assertThrows(BusinessException.class, () -> classes.update(update));
    }

    @Test
    void updatesRejectEmptyPayloadAndMissingRecords()
    {
        var collegeDTO = new UpdateCollegeDTO();
        collegeDTO.setId(1L);
        assertThrows(BusinessException.class, () -> colleges.update(collegeDTO));
        var classDTO = new UpdateClassDTO();
        classDTO.setId(1L);
        assertThrows(BusinessException.class, () -> classes.update(classDTO));
        assertThrows(BusinessException.class, () -> classes.delete(999L));
        assertThrows(BusinessException.class, () -> colleges.selectById(-1L));
    }
}
