/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80411 (8.4.11)
 Source Host           : localhost:3306
 Source Schema         : Employment

 Target Server Type    : MySQL
 Target Server Version : 80411 (8.4.11)
 File Encoding         : 65001

 Date: 26/10/2026 14:43:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for basic_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `basic_knowledge`;
CREATE TABLE `basic_knowledge` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `answer` text NOT NULL COMMENT '回答',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '是否生效：0未生效，1生效',
  PRIMARY KEY (`id`),
  KEY `idx_basic_knowledge_status` (`status`),
  CONSTRAINT `ck_basic_knowledge_status` CHECK ((`status` in (0,1)))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI基础知识表';

-- ----------------------------
-- Records of basic_knowledge
-- ----------------------------
BEGIN;
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (1, '网站简介', '本网站是高校毕业生就业信息备案系统，提供学生端和管理端。学生可以维护个人信息、提交就业备案、查看通知公告、填写求职条件获取岗位推荐以及使用 AI 就业问答；管理端用于维护学校基础资料、账号、工作信息和审核备案。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (2, '学生端主要功能和入口', '登录学生端后，可通过导航栏进入个人中心、就业备案、岗位推荐和通知公告。首页服务列表提供服务入口，可通过“添加服务”调整收藏。AI 就业问答可从服务列表或岗位推荐页面进入。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (3, '个人资料如何修改', '在学生端导航栏进入个人中心，再进入修改个人信息页面。修改范围以页面开放的字段为准，学校维护的关联资料应联系学校处理。登录账号不能自行修改。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (4, '就业备案审核流程', '学生提交备案后进入待辅导员初审阶段，辅导员通过后进入院系复审，院系通过后归档。归档是备案状态，不是学生需要执行的操作。请在就业备案页面查看实际进度。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (5, '备案提交后能否修改', '备案提交后只能查看，不能直接修改原记录。审核人员可按权限打回；打回后请查看审核意见并联系负责辅导员，后续操作以页面实际提供的入口为准。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (6, '审核中能否重复申请', '存在待辅导员初审或待院系复审的备案时，不能提交新的申请。应等待当前审核结束，再根据状态和页面入口办理。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (7, '归档后如何申请变更', '已有备案归档后，可以发起新的变更申请，填写变更原因并重新提交资料。旧记录保留，新的申请重新进入审核流程，可通过历史记录查看过往备案。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (8, '备案被打回是什么状态', '备案打回状态为“已打回”，状态码是40，不是0。其他主要状态为10待辅导员初审、20待院系复审、30已归档。学生应以页面显示的状态和审核意见为准。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (9, '如何查看学校公告', '学生可在首页通知公告区域查看公告摘要，点击公告查看详情，或通过“查看全部”和导航栏进入公告列表。学生端仅展示已发布公告，不展示草稿或已撤回公告。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (10, '岗位推荐需要填写什么', '岗位推荐必填项包括最高学历、工作年限、掌握技能、期望岗位类别、期望城市、求职类型和最看重的因素。应届生工作年限填写0。专业、薪资、工作方式、休息制度、出差及外包偏好、其他要求可以按实际情况选填。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (11, '如何选择工作技能', '掌握技能从学校维护的技能库中选择。默认显示前20个技能，可以输入关键词搜索并多选，不能自行创建技能。如果缺少所需技能，请联系管理人员补充。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (12, '不限和未填写有什么区别', '不限表示明确接受该条件下的全部选项；未填写表示没有表达偏好，不应替学生推断。期望城市选择不限时，不能同时选择具体城市。不限条件不应开启必须满足开关。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (13, '必须满足开关有什么作用', '在岗位推荐表单中，城市、工作方式、休息制度、出差和外包等条件可设置必须满足。开启后作为硬性筛选条件；未开启时用于偏好匹配。填写最低可接受月薪后，该值作为薪资底线。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (14, '如何查看岗位匹配结果', '填写表单后点击预览求职条件，再点击获取匹配结果。结果展示匹配度、岗位类别、城市、技能和薪资等信息。匹配度用于辅助比较，不代表录用概率或企业保证。查看详情可以对照本次提交条件和工作要求。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (15, '学历和工作年限如何匹配', '学历按达到或高于岗位要求判定，例如本科可以匹配专科要求，硕士可以匹配本科要求。工作年限按是否达到要求判定，超过岗位所需年限不会因此扣分。技能按岗位所需技能的覆盖情况计算。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (16, '如何联系推荐的公司', '在岗位匹配结果中点击联系公司，会弹窗显示公司名称和联系方式，不会自动跳转邮箱或拨号。若联系方式未提供，应向管理员核实。带测试标识的公司和 example.com 示例邮箱仅用于开发演示，不能作为真实招聘联系方式。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (17, '岗位详情比较的是哪次填写的信息', '岗位详情中的我的条件使用生成这次匹配结果时提交的表单快照。如果后来修改了表单，旧结果不会自动重新计算，需要重新提交后查看新的匹配结果。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (18, 'AI就业问答可以咨询什么', '可咨询网站功能使用、就业备案流程、岗位选择、技能准备、简历与面试等就业相关问题。涉及个人审核进度、账号权限或具体企业资料时，应以系统实际记录及学校核实结果为准，不应将推测作为事实。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (19, '管理端能维护哪些资料', '管理端提供账号、学生、辅导员、院系管理员、学院、专业、班级、辅导员班级关联、公告、工作、工作技能和AI提示词等管理入口。具体操作受账号权限和管理范围限制，不是所有教职工都能查看或审核全部学生。', 1);
INSERT INTO `basic_knowledge` (`id`, `title`, `answer`, `status`) VALUES (20, 'AI提示词管理中的生效状态', '管理端AI提示词管理维护基础知识的标题、回答和是否生效。0表示未生效，1表示已生效。基础知识的生效查询仅返回已生效记录；维护后是否用于回答取决于AI服务的实际接入逻辑。', 1);
COMMIT;

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_code` varchar(30) NOT NULL COMMENT '班级编号',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `enrollment_year` smallint DEFAULT NULL COMMENT '入学年份',
  `student_count` int NOT NULL DEFAULT '0' COMMENT '学生人数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_code` (`class_code`),
  KEY `idx_college_id` (`college_id`),
  KEY `fk_class_major` (`major_id`),
  CONSTRAINT `fk_class_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级信息表';

-- ----------------------------
-- Records of class_info
-- ----------------------------
BEGIN;
INSERT INTO `class_info` (`id`, `class_code`, `class_name`, `college_id`, `enrollment_year`, `student_count`, `created_at`, `updated_at`, `major_id`) VALUES (1, 'CS2301', '计算机2301班', 1, 2023, 0, '2026-10-14 22:26:36', '2026-10-23 14:21:12', 2);
INSERT INTO `class_info` (`id`, `class_code`, `class_name`, `college_id`, `enrollment_year`, `student_count`, `created_at`, `updated_at`, `major_id`) VALUES (2, 'CS2302', '计算机2302班', 1, 2023, 0, '2026-10-14 22:26:36', '2026-10-23 14:21:12', 3);
INSERT INTO `class_info` (`id`, `class_code`, `class_name`, `college_id`, `enrollment_year`, `student_count`, `created_at`, `updated_at`, `major_id`) VALUES (3, 'AI2401', '人工智能2401班', 1, 2024, 0, '2026-10-14 22:26:36', '2026-10-23 14:21:12', 1);
INSERT INTO `class_info` (`id`, `class_code`, `class_name`, `college_id`, `enrollment_year`, `student_count`, `created_at`, `updated_at`, `major_id`) VALUES (4, 'xxcg1', '物联网1班', 3, 2026, 40, '2026-10-23 15:23:42', '2026-10-23 15:23:42', 4);
COMMIT;

-- ----------------------------
-- Table structure for college
-- ----------------------------
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学院ID',
  `college_code` varchar(20) NOT NULL COMMENT '学院编号',
  `college_name` varchar(100) NOT NULL COMMENT '学院名称',
  `dean_name` varchar(50) DEFAULT NULL COMMENT '院长姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '学院联系电话',
  `description` varchar(500) DEFAULT NULL COMMENT '学院简介',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_college_code` (`college_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学院信息表';

-- ----------------------------
-- Records of college
-- ----------------------------
BEGIN;
INSERT INTO `college` (`id`, `college_code`, `college_name`, `dean_name`, `phone`, `description`, `created_at`, `updated_at`) VALUES (1, 'CS', '计算机学院', NULL, NULL, NULL, '2026-10-14 22:26:36', '2026-10-14 22:26:36');
INSERT INTO `college` (`id`, `college_code`, `college_name`, `dean_name`, `phone`, `description`, `created_at`, `updated_at`) VALUES (2, 'AI', '人工智能学院', NULL, NULL, NULL, '2026-10-14 22:26:36', '2026-10-14 22:26:36');
INSERT INTO `college` (`id`, `college_code`, `college_name`, `dean_name`, `phone`, `description`, `created_at`, `updated_at`) VALUES (3, 'WZBC01', '信息工程', 'James', '11', '', '2026-10-18 15:27:17', '2026-10-18 15:27:17');
COMMIT;

-- ----------------------------
-- Table structure for college_admin
-- ----------------------------
DROP TABLE IF EXISTS `college_admin`;
CREATE TABLE `college_admin` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '管理员资料ID',
  `user_id` bigint unsigned NOT NULL COMMENT '关联sys_user的账号ID',
  `name` varchar(50) NOT NULL COMMENT '管理员姓名',
  `college_id` bigint unsigned NOT NULL COMMENT '所属学院ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_college_id` (`college_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='院系管理员表';

-- ----------------------------
-- Records of college_admin
-- ----------------------------
BEGIN;
INSERT INTO `college_admin` (`id`, `user_id`, `name`, `college_id`, `created_at`, `updated_at`) VALUES (1, 8, '叶鹏', 3, '2026-10-18 16:15:01', '2026-10-18 16:15:01');
INSERT INTO `college_admin` (`id`, `user_id`, `name`, `college_id`, `created_at`, `updated_at`) VALUES (2, 11, '张三', 1, '2026-10-23 21:29:58', '2026-10-23 21:29:58');
COMMIT;

-- ----------------------------
-- Table structure for counselor
-- ----------------------------
DROP TABLE IF EXISTS `counselor`;
CREATE TABLE `counselor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员ID',
  `counselor_no` varchar(30) NOT NULL COMMENT '辅导员工号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` tinyint DEFAULT '0' COMMENT '0未知 1男 2女',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1在职 0离职',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_counselor_no` (`counselor_no`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='辅导员信息表';

-- ----------------------------
-- Records of counselor
-- ----------------------------
BEGIN;
INSERT INTO `counselor` (`id`, `counselor_no`, `name`, `gender`, `phone`, `email`, `hire_date`, `status`, `created_at`, `updated_at`) VALUES (1, 'T001', '王老师', 1, '13800000001', NULL, NULL, 1, '2026-10-14 22:26:36', '2026-10-14 22:26:36');
INSERT INTO `counselor` (`id`, `counselor_no`, `name`, `gender`, `phone`, `email`, `hire_date`, `status`, `created_at`, `updated_at`) VALUES (2, 'T002', '李老师', 2, '13800000002', NULL, NULL, 1, '2026-10-14 22:26:36', '2026-10-14 22:26:36');
INSERT INTO `counselor` (`id`, `counselor_no`, `name`, `gender`, `phone`, `email`, `hire_date`, `status`, `created_at`, `updated_at`) VALUES (3, '20260110', '叶A', 1, '13758202202', 'jamesrony12138@gmail.com', '2026-10-14', 1, '2026-10-18 15:14:06', '2026-10-18 15:14:06');
INSERT INTO `counselor` (`id`, `counselor_no`, `name`, `gender`, `phone`, `email`, `hire_date`, `status`, `created_at`, `updated_at`) VALUES (4, '20260111', '叶B', 1, '13758202202', 'jamesrony12138@gmail.com', '2026-10-13', 1, '2026-10-18 15:17:37', '2026-10-18 15:17:37');
COMMIT;

-- ----------------------------
-- Table structure for counselor_class
-- ----------------------------
DROP TABLE IF EXISTS `counselor_class`;
CREATE TABLE `counselor_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `role_type` tinyint NOT NULL DEFAULT '1' COMMENT '1主辅导员 2协管辅导员',
  `assigned_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_counselor_class` (`counselor_id`,`class_id`),
  KEY `idx_class_id` (`class_id`),
  CONSTRAINT `fk_cc_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`),
  CONSTRAINT `fk_cc_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='辅导员班级管理关系表';

-- ----------------------------
-- Records of counselor_class
-- ----------------------------
BEGIN;
INSERT INTO `counselor_class` (`id`, `counselor_id`, `class_id`, `role_type`, `assigned_at`) VALUES (1, 1, 1, 1, '2026-10-14 22:26:36');
INSERT INTO `counselor_class` (`id`, `counselor_id`, `class_id`, `role_type`, `assigned_at`) VALUES (2, 1, 2, 1, '2026-10-14 22:26:36');
INSERT INTO `counselor_class` (`id`, `counselor_id`, `class_id`, `role_type`, `assigned_at`) VALUES (4, 2, 1, 2, '2026-10-14 22:26:36');
INSERT INTO `counselor_class` (`id`, `counselor_id`, `class_id`, `role_type`, `assigned_at`) VALUES (5, 3, 3, 1, '2026-10-23 10:39:30');
COMMIT;

-- ----------------------------
-- Table structure for employment_audit_record
-- ----------------------------
DROP TABLE IF EXISTS `employment_audit_record`;
CREATE TABLE `employment_audit_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录主键',
  `filing_id` bigint NOT NULL COMMENT '对应本次提交的备案ID',
  `audit_stage` tinyint NOT NULL COMMENT '1初审阶段 2复审阶段',
  `auditor_id` bigint NOT NULL COMMENT '审核人ID，由业务关联管理端账号',
  `auditor_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核人姓名快照',
  `auditor_role` tinyint NOT NULL COMMENT '1辅导员 2院系审核人员',
  `audit_result` tinyint NOT NULL COMMENT '1通过 2打回',
  `audit_opinion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见，打回时必填',
  `from_status` tinyint NOT NULL COMMENT '审核前备案状态',
  `to_status` tinyint NOT NULL COMMENT '审核后备案状态',
  `audited_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '审核时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_audit_filing_stage` (`filing_id`,`audit_stage`),
  KEY `idx_audit_filing_time` (`filing_id`,`audited_at`,`id`),
  KEY `idx_audit_auditor_time` (`auditor_id`,`audited_at`),
  CONSTRAINT `fk_audit_filing` FOREIGN KEY (`filing_id`) REFERENCES `employment_filing` (`id`),
  CONSTRAINT `chk_audit_result` CHECK ((`audit_result` in (1,2))),
  CONSTRAINT `chk_audit_return_opinion` CHECK (((`audit_result` <> 2) or ((`audit_opinion` is not null) and (char_length(trim(`audit_opinion`)) > 0)))),
  CONSTRAINT `chk_audit_role` CHECK ((`auditor_role` in (1,2))),
  CONSTRAINT `chk_audit_stage` CHECK ((`audit_stage` in (1,2))),
  CONSTRAINT `chk_audit_transition` CHECK ((((`audit_result` = 1) and (`audit_stage` = 1) and (`auditor_role` = 1) and (`from_status` = 10) and (`to_status` = 20)) or ((`audit_result` = 1) and (`audit_stage` = 2) and (`auditor_role` = 2) and (`from_status` = 20) and (`to_status` = 30)) or ((`audit_result` = 2) and (`auditor_role` = 1) and (`to_status` = 40) and (((`audit_stage` = 1) and (`from_status` = 10)) or ((`audit_stage` = 2) and (`from_status` = 20))))))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就业审核记录表，审核历史只追加';

-- ----------------------------
-- Records of employment_audit_record
-- ----------------------------
BEGIN;
INSERT INTO `employment_audit_record` (`id`, `filing_id`, `audit_stage`, `auditor_id`, `auditor_name`, `auditor_role`, `audit_result`, `audit_opinion`, `from_status`, `to_status`, `audited_at`) VALUES (1, 3, 1, 1, '测试教师一1', 1, 1, '测试数据：模拟初审通过，等待院系复审', 10, 20, '2026-10-23 09:30:00.000');
INSERT INTO `employment_audit_record` (`id`, `filing_id`, `audit_stage`, `auditor_id`, `auditor_name`, `auditor_role`, `audit_result`, `audit_opinion`, `from_status`, `to_status`, `audited_at`) VALUES (2, 4, 1, 1, '测试教师一1', 1, 2, '测试数据：模拟打回，请补充就业证明材料后重新申请', 10, 40, '2026-10-23 09:30:00.000');
INSERT INTO `employment_audit_record` (`id`, `filing_id`, `audit_stage`, `auditor_id`, `auditor_name`, `auditor_role`, `audit_result`, `audit_opinion`, `from_status`, `to_status`, `audited_at`) VALUES (4, 2, 1, 1, '测试教师一1', 1, 1, '', 10, 20, '2026-10-23 21:20:24.328');
INSERT INTO `employment_audit_record` (`id`, `filing_id`, `audit_stage`, `auditor_id`, `auditor_name`, `auditor_role`, `audit_result`, `audit_opinion`, `from_status`, `to_status`, `audited_at`) VALUES (5, 3, 2, 11, '张三', 2, 1, '', 20, 30, '2026-10-23 21:30:45.936');
INSERT INTO `employment_audit_record` (`id`, `filing_id`, `audit_stage`, `auditor_id`, `auditor_name`, `auditor_role`, `audit_result`, `audit_opinion`, `from_status`, `to_status`, `audited_at`) VALUES (6, 1, 2, 11, '张三', 2, 1, '', 20, 30, '2026-10-23 21:31:13.599');
COMMIT;

-- ----------------------------
-- Table structure for employment_filing
-- ----------------------------
DROP TABLE IF EXISTS `employment_filing`;
CREATE TABLE `employment_filing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '备案主键',
  `application_no` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务端生成的申请编号',
  `student_uuid` bigint NOT NULL COMMENT '关联student.uuid，由业务校验',
  `student_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提交时学号快照',
  `student_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提交时姓名快照',
  `college` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学院快照',
  `major` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业快照',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `class_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称快照',
  `graduation_year` smallint NOT NULL COMMENT '毕业年份',
  `application_type` tinyint NOT NULL DEFAULT '1' COMMENT '1首次备案 2变更申请 3打回重提',
  `previous_filing_id` bigint DEFAULT NULL COMMENT '变更或重提所关联的前一条备案',
  `change_reason` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '变更原因',
  `destination_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '毕业去向字典编码，具体分类待定',
  `company_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位名称',
  `company_credit_code` varchar(18) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位统一社会信用代码',
  `company_industry` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位行业字典编码',
  `company_nature` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位性质字典编码',
  `job_title` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '就业岗位',
  `work_province` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作省份',
  `work_city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作城市',
  `work_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作详细地址',
  `monthly_salary` decimal(12,2) DEFAULT NULL COMMENT '月薪元，未填为NULL',
  `signed_date` date DEFAULT NULL COMMENT '签约日期',
  `contact_phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话快照',
  `contact_email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系邮箱快照',
  `destination_details` json DEFAULT NULL COMMENT '其他去向详情对象，如升学院校专业',
  `materials` json DEFAULT NULL COMMENT '材料引用数组，保存fileId、name、type等',
  `remark` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学生备注',
  `status` tinyint NOT NULL DEFAULT '10' COMMENT '10待辅导员初审 20待院系复审 30已归档 40已打回',
  `submitted_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '提交时间，不随审核更新',
  `archived_at` datetime(3) DEFAULT NULL COMMENT '复审通过后自动进入归档状态的时间',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `pending_student_uuid` bigint GENERATED ALWAYS AS ((case when (`status` in (10,20)) then `student_uuid` else NULL end)) STORED COMMENT '唯一约束防止同一学生同时存在多条审核中的备案',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_filing_application_no` (`application_no`),
  UNIQUE KEY `uk_filing_pending_student` (`pending_student_uuid`),
  KEY `idx_filing_student_latest` (`student_uuid`,`submitted_at` DESC,`id` DESC),
  KEY `idx_filing_audit_queue` (`status`,`class_id`,`submitted_at`),
  KEY `idx_filing_graduation_status` (`graduation_year`,`status`),
  KEY `idx_filing_previous` (`previous_filing_id`),
  CONSTRAINT `chk_filing_archive_time` CHECK ((((`status` = 30) and (`archived_at` is not null)) or ((`status` <> 30) and (`archived_at` is null)))),
  CONSTRAINT `chk_filing_change_reason` CHECK (((`application_type` <> 2) or ((`change_reason` is not null) and (char_length(trim(`change_reason`)) > 0)))),
  CONSTRAINT `chk_filing_details` CHECK (((`destination_details` is null) or (json_type(`destination_details`) = _utf8mb4'OBJECT'))),
  CONSTRAINT `chk_filing_materials` CHECK ((json_type(`materials`) = _utf8mb4'ARRAY')),
  CONSTRAINT `chk_filing_previous` CHECK ((((`application_type` = 1) and (`previous_filing_id` is null)) or ((`application_type` in (2,3)) and (`previous_filing_id` is not null)))),
  CONSTRAINT `chk_filing_salary` CHECK (((`monthly_salary` is null) or (`monthly_salary` >= 0))),
  CONSTRAINT `chk_filing_status` CHECK ((`status` in (10,20,30,40))),
  CONSTRAINT `chk_filing_type` CHECK ((`application_type` in (1,2,3))),
  CONSTRAINT `chk_filing_version` CHECK ((`version` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就业备案表，每次提交独立留存';

-- ----------------------------
-- Records of employment_filing
-- ----------------------------
BEGIN;
INSERT INTO `employment_filing` (`id`, `application_no`, `student_uuid`, `student_no`, `student_name`, `college`, `major`, `class_id`, `class_name`, `graduation_year`, `application_type`, `previous_filing_id`, `change_reason`, `destination_type`, `company_name`, `company_credit_code`, `company_industry`, `company_nature`, `job_title`, `work_province`, `work_city`, `work_address`, `monthly_salary`, `signed_date`, `contact_phone`, `contact_email`, `destination_details`, `materials`, `remark`, `status`, `submitted_at`, `archived_at`, `version`, `created_at`, `updated_at`) VALUES (1, 'JY202610160001', 3, '25230820025', 'james', '信息工程学院', '计算机科学与技术', 1, '计算机科学与技术1班', 2030, 1, NULL, NULL, NULL, '字节跳动', '911101085923662400', NULL, NULL, '后端开发', '浙江', '杭州', '浙江省杭州市萧山区星河景庭公寓', 9000.00, '2026-10-28', '13758202201', 'jamesrony98@gmail.com', '{}', '[]', '123', 30, '2026-10-16 09:45:19.060', '2026-10-23 21:31:13.594', 1, '2026-10-16 09:45:19.060', '2026-10-23 21:31:13.594');
INSERT INTO `employment_filing` (`id`, `application_no`, `student_uuid`, `student_no`, `student_name`, `college`, `major`, `class_id`, `class_name`, `graduation_year`, `application_type`, `previous_filing_id`, `change_reason`, `destination_type`, `company_name`, `company_credit_code`, `company_industry`, `company_nature`, `job_title`, `work_province`, `work_city`, `work_address`, `monthly_salary`, `signed_date`, `contact_phone`, `contact_email`, `destination_details`, `materials`, `remark`, `status`, `submitted_at`, `archived_at`, `version`, `created_at`, `updated_at`) VALUES (2, 'TEST20261023STU1', 1, '2026001001', '张三', '计算机学院', '计算机科学与技术', 1, '计算机2301班', 2030, 1, NULL, NULL, NULL, '测试企业（学生1）', NULL, NULL, NULL, '软件开发工程师', '浙江省', '温州市', NULL, 6000.00, NULL, NULL, NULL, NULL, '[]', '测试数据：用于备案审核页面联调，非真实就业信息', 20, '2026-10-23 09:00:00.000', NULL, 1, '2026-10-23 15:33:45.043', '2026-10-23 21:20:24.317');
INSERT INTO `employment_filing` (`id`, `application_no`, `student_uuid`, `student_no`, `student_name`, `college`, `major`, `class_id`, `class_name`, `graduation_year`, `application_type`, `previous_filing_id`, `change_reason`, `destination_type`, `company_name`, `company_credit_code`, `company_industry`, `company_nature`, `job_title`, `work_province`, `work_city`, `work_address`, `monthly_salary`, `signed_date`, `contact_phone`, `contact_email`, `destination_details`, `materials`, `remark`, `status`, `submitted_at`, `archived_at`, `version`, `created_at`, `updated_at`) VALUES (3, 'TEST20261023STU2', 2, '2026001002', '李四', '计算机学院', '计算机科学与技术', 1, '计算机2301班', 2030, 1, NULL, NULL, NULL, '测试企业（学生2）', NULL, NULL, NULL, '软件开发工程师', '浙江省', '温州市', NULL, 6000.00, NULL, NULL, NULL, NULL, '[]', '测试数据：用于备案审核页面联调，非真实就业信息', 30, '2026-10-23 09:00:00.000', '2026-10-23 21:30:45.929', 2, '2026-10-23 15:33:45.043', '2026-10-23 21:30:45.929');
INSERT INTO `employment_filing` (`id`, `application_no`, `student_uuid`, `student_no`, `student_name`, `college`, `major`, `class_id`, `class_name`, `graduation_year`, `application_type`, `previous_filing_id`, `change_reason`, `destination_type`, `company_name`, `company_credit_code`, `company_industry`, `company_nature`, `job_title`, `work_province`, `work_city`, `work_address`, `monthly_salary`, `signed_date`, `contact_phone`, `contact_email`, `destination_details`, `materials`, `remark`, `status`, `submitted_at`, `archived_at`, `version`, `created_at`, `updated_at`) VALUES (4, 'TEST20261023STU4', 4, '25230820026', 'rony', '计算机学院', '软件工程', 2, '计算机2302班', 2030, 1, NULL, NULL, NULL, '测试企业（学生4）', NULL, NULL, NULL, '软件测试工程师', '浙江省', '温州市', NULL, 6000.00, NULL, NULL, NULL, NULL, '[]', '测试数据：用于备案审核页面联调，非真实就业信息', 40, '2026-10-23 09:00:00.000', NULL, 1, '2026-10-23 15:33:45.043', '2026-10-23 15:33:45.043');
INSERT INTO `employment_filing` (`id`, `application_no`, `student_uuid`, `student_no`, `student_name`, `college`, `major`, `class_id`, `class_name`, `graduation_year`, `application_type`, `previous_filing_id`, `change_reason`, `destination_type`, `company_name`, `company_credit_code`, `company_industry`, `company_nature`, `job_title`, `work_province`, `work_city`, `work_address`, `monthly_salary`, `signed_date`, `contact_phone`, `contact_email`, `destination_details`, `materials`, `remark`, `status`, `submitted_at`, `archived_at`, `version`, `created_at`, `updated_at`) VALUES (5, 'TEST20261023STU5', 5, '25230820021', 'J2', '信息工程', '物联网', 4, '物联网1班', 2030, 1, NULL, NULL, NULL, '测试企业（学生5）', NULL, NULL, NULL, '软件开发工程师', '浙江省', '温州市', NULL, 6000.00, NULL, NULL, NULL, NULL, '[]', '测试数据：用于备案审核页面联调，非真实就业信息', 10, '2026-10-23 09:00:00.000', NULL, 0, '2026-10-23 15:33:45.043', '2026-10-23 15:33:45.043');
COMMIT;

-- ----------------------------
-- Table structure for employment_filing_sequence
-- ----------------------------
DROP TABLE IF EXISTS `employment_filing_sequence`;
CREATE TABLE `employment_filing_sequence` (
  `application_date` char(8) NOT NULL COMMENT '申请日期 yyyyMMdd，上海时区',
  `sequence_no` int NOT NULL COMMENT '当日已分配流水号',
  PRIMARY KEY (`application_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='就业备案每日申请编号计数器';

-- ----------------------------
-- Records of employment_filing_sequence
-- ----------------------------
BEGIN;
INSERT INTO `employment_filing_sequence` (`application_date`, `sequence_no`) VALUES ('20261016', 1);
COMMIT;

-- ----------------------------
-- Table structure for major
-- ----------------------------
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `major_name` varchar(100) NOT NULL COMMENT '专业名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_major_college_name` (`college_id`,`major_name`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业表';

-- ----------------------------
-- Records of major
-- ----------------------------
BEGIN;
INSERT INTO `major` (`id`, `college_id`, `major_name`, `created_at`, `updated_at`) VALUES (1, 1, '人工智能', '2026-10-23 14:21:12', '2026-10-23 14:21:12');
INSERT INTO `major` (`id`, `college_id`, `major_name`, `created_at`, `updated_at`) VALUES (2, 1, '计算机科学与技术', '2026-10-23 14:21:12', '2026-10-23 14:21:12');
INSERT INTO `major` (`id`, `college_id`, `major_name`, `created_at`, `updated_at`) VALUES (3, 1, '软件工程', '2026-10-23 14:21:12', '2026-10-23 14:21:12');
INSERT INTO `major` (`id`, `college_id`, `major_name`, `created_at`, `updated_at`) VALUES (4, 3, '物联网', '2026-10-23 15:23:11', '2026-10-23 15:23:11');
COMMIT;

-- ----------------------------
-- Table structure for service_entry
-- ----------------------------
DROP TABLE IF EXISTS `service_entry`;
CREATE TABLE `service_entry` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `icon_url` varchar(500) NOT NULL,
  `url` varchar(500) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_service_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生首页服务配置';

-- ----------------------------
-- Records of service_entry
-- ----------------------------
BEGIN;
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (1, '岗位推荐', '/services/jobs.svg', '/recommendations', 10, 1, '2026-10-26 13:42:56.376', '2026-10-26 13:42:56.376');
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (2, '个人中心', '/services/profile.svg', '/profile', 20, 1, '2026-10-26 13:42:56.379', '2026-10-26 13:42:56.379');
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (3, '就业备案', '/services/filing.svg', '/filing', 30, 1, '2026-10-26 13:42:56.379', '2026-10-26 13:42:56.379');
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (4, '校园招聘', '/services/jobs.svg', '/jobs', 40, 1, '2026-10-26 13:42:56.379', '2026-10-26 13:42:56.379');
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (5, '通知公告', '/services/notices.svg', '/notices', 50, 1, '2026-10-26 13:42:56.380', '2026-10-26 13:42:56.380');
INSERT INTO `service_entry` (`id`, `name`, `icon_url`, `url`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (6, 'AI 就业问答', '/services/chat.svg', '/ai-chat', 60, 1, '2026-10-26 13:42:56.380', '2026-10-26 13:42:56.380');
COMMIT;

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `uuid` bigint NOT NULL AUTO_INCREMENT COMMENT '学生主键ID',
  `student_no` varchar(20) NOT NULL COMMENT '学号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` tinyint DEFAULT '0' COMMENT '0未知 1男 2女',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `enrollment_year` smallint DEFAULT NULL COMMENT '入学年份',
  `graduation_year` smallint DEFAULT NULL COMMENT '预计毕业年份',
  `education_level` varchar(20) DEFAULT NULL COMMENT '学历层次',
  `student_status` tinyint NOT NULL DEFAULT '1' COMMENT '1在读 2休学 3毕业 4退学',
  `address` varchar(255) DEFAULT NULL COMMENT '家庭住址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `class_id` bigint DEFAULT NULL COMMENT '所属班级ID',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `student_no` (`student_no`),
  KEY `idx_student_class` (`class_id`),
  KEY `fk_student_major` (`major_id`),
  KEY `fk_student_college` (`college_id`),
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`),
  CONSTRAINT `fk_student_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`),
  CONSTRAINT `fk_student_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='大学学生信息表';

-- ----------------------------
-- Records of student
-- ----------------------------
BEGIN;
INSERT INTO `student` (`uuid`, `student_no`, `name`, `gender`, `birth_date`, `id_card`, `phone`, `email`, `enrollment_year`, `graduation_year`, `education_level`, `student_status`, `address`, `created_at`, `updated_at`, `class_id`, `major_id`, `college_id`) VALUES (1, '2026001001', '张三', 1, '2005-01-11', '110101200501011234', NULL, NULL, 2026, 2030, '本科', 1, NULL, '2026-10-15 10:36:59', '2026-10-23 14:34:54', 1, 2, 1);
INSERT INTO `student` (`uuid`, `student_no`, `name`, `gender`, `birth_date`, `id_card`, `phone`, `email`, `enrollment_year`, `graduation_year`, `education_level`, `student_status`, `address`, `created_at`, `updated_at`, `class_id`, `major_id`, `college_id`) VALUES (2, '2026001002', '李四', 1, '2005-01-11', '110101200501111234', NULL, NULL, 2026, 2030, '本科', 1, NULL, '2026-10-15 10:42:02', '2026-10-23 14:34:54', 1, 2, 1);
INSERT INTO `student` (`uuid`, `student_no`, `name`, `gender`, `birth_date`, `id_card`, `phone`, `email`, `enrollment_year`, `graduation_year`, `education_level`, `student_status`, `address`, `created_at`, `updated_at`, `class_id`, `major_id`, `college_id`) VALUES (3, '25230820025', 'james', 1, '2004-06-08', '330109200406080078', '13758202201', 'jamesrony98@gmail.com', 2026, 2030, '本科', 1, NULL, '2026-10-15 13:03:21', '2026-10-23 14:34:54', 1, 2, 1);
INSERT INTO `student` (`uuid`, `student_no`, `name`, `gender`, `birth_date`, `id_card`, `phone`, `email`, `enrollment_year`, `graduation_year`, `education_level`, `student_status`, `address`, `created_at`, `updated_at`, `class_id`, `major_id`, `college_id`) VALUES (4, '25230820026', 'rony', 1, '2004-10-25', '330109200410250078', NULL, NULL, 2026, 2030, '本科', 1, NULL, '2026-10-23 15:06:03', '2026-10-23 15:06:03', 2, 3, 1);
INSERT INTO `student` (`uuid`, `student_no`, `name`, `gender`, `birth_date`, `id_card`, `phone`, `email`, `enrollment_year`, `graduation_year`, `education_level`, `student_status`, `address`, `created_at`, `updated_at`, `class_id`, `major_id`, `college_id`) VALUES (5, '25230820021', 'J2', 1, '2004-10-23', '330109200410230078', NULL, NULL, 1900, 2030, '本科', 1, NULL, '2026-10-23 15:24:26', '2026-10-23 15:24:26', 4, 4, 3);
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(50) NOT NULL,
  `path` varchar(200) DEFAULT NULL,
  `icon` varchar(50) DEFAULT NULL,
  `menu_type` varchar(20) NOT NULL,
  `permission` varchar(100) NOT NULL,
  `client_type` varchar(20) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_permission` (`permission`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表\n';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (1, 0, '工作台', '/', 'Collection', 'MENU', 'admin:home:view', 'ADMIN', 0, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (2, 0, '个人中心', '/profile', 'Collection', 'MENU', 'admin:profile:view', 'ADMIN', 1, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (3, 0, '学生管理', '/students', 'Collection', 'MENU', 'admin:students:view', 'ADMIN', 2, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (4, 0, '班级管理', '/classes', 'Collection', 'MENU', 'admin:classes:view', 'ADMIN', 3, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (5, 0, '学院管理', '/colleges', 'Collection', 'MENU', 'admin:colleges:view', 'ADMIN', 4, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (6, 0, '专业管理', '/majors', 'Collection', 'MENU', 'admin:majors:view', 'ADMIN', 5, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (7, 0, '辅导员管理', '/counselors', 'Collection', 'MENU', 'admin:counselors:view', 'ADMIN', 6, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (8, 0, '辅导员班级管理', '/counselor-classes', 'Collection', 'MENU', 'admin:counselor-classes:view', 'ADMIN', 7, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (9, 0, '院系管理员', '/department-admins', 'Collection', 'MENU', 'admin:department-admins:view', 'ADMIN', 8, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (10, 0, '账号管理', '/accounts', 'Collection', 'MENU', 'admin:accounts:view', 'ADMIN', 9, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (11, 0, '备案审核', '/filings', 'Collection', 'MENU', 'admin:filings:view', 'ADMIN', 10, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (12, 0, '公告管理', '/notices', 'Collection', 'MENU', 'admin:notices:view', 'ADMIN', 11, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (13, 0, '工作管理', '/works', 'Collection', 'MENU', 'admin:works:view', 'ADMIN', 12, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (14, 0, '工作技能管理', '/work-skills', 'Collection', 'MENU', 'admin:work-skills:view', 'ADMIN', 13, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (15, 0, 'AI提示词管理', '/ai-knowledge', 'Collection', 'MENU', 'admin:ai-knowledge:view', 'ADMIN', 14, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (16, 0, '菜单管理', '/menus', 'Collection', 'MENU', 'admin:menus:view', 'ADMIN', 15, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (17, 0, '首页', '/', 'Collection', 'MENU', 'student:home:view', 'STUDENT', 0, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (18, 0, '个人中心', '/profile', 'Collection', 'MENU', 'student:profile:view', 'STUDENT', 1, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (19, 0, '就业备案', '/filing', 'Collection', 'MENU', 'student:filing:view', 'STUDENT', 2, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (20, 0, '校园招聘', '/jobs', 'Collection', 'MENU', 'student:jobs:view', 'STUDENT', 3, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (21, 0, '岗位推荐', '/recommendations', 'Collection', 'MENU', 'student:recommendations:view', 'STUDENT', 4, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (22, 0, '通知公告', '/notices', 'Collection', 'MENU', 'student:notices:view', 'STUDENT', 5, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (23, 0, 'AI就业问答', '/ai-chat', 'Collection', 'MENU', 'student:ai-chat:view', 'STUDENT', 6, 1);
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `menu_type`, `permission`, `client_type`, `sort_order`, `status`) VALUES (24, 0, '服务管理', '/services', 'grid', 'MENU', 'service:manage', 'ADMIN', 150, 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` longtext NOT NULL COMMENT '公告正文，支持富文本',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '公告类型：1系统公告 2就业政策 3备案通知 4其他',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1已发布 2已撤回',
  `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶：0否 1是',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID',
  `publisher_name` varchar(100) DEFAULT NULL COMMENT '发布人姓名',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `publish_time` datetime(3) DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_publish` (`status`,`publish_time`),
  KEY `idx_top` (`is_top`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (1, '【测试】毕业生就业备案填报说明', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请核对个人资料、班级及联系方式后填写就业备案。提交后资料只读，请在提交前仔细检查。', 3, 1, 1, 3, '测试管理员', 0, '2026-10-24 09:00:00.000', '2026-10-24 09:00:00.000', '2026-10-24 09:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (2, '【测试】就业备案材料准备提示', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请提前整理与就业去向相关的证明材料，并向辅导员确认具体要求。请勿上传与备案无关的信息。', 3, 1, 1, 3, '测试管理员', 0, '2026-10-24 08:00:00.000', '2026-10-24 08:00:00.000', '2026-10-24 08:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (3, '【测试】个人联系方式核对提醒', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请进入个人中心核对手机号和邮箱，确保学校能够及时联系。', 1, 1, 0, 3, '测试管理员', 0, '2026-10-24 07:00:00.000', '2026-10-24 07:00:00.000', '2026-10-24 07:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (4, '【测试】就业信息真实性提醒', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请如实填写就业单位、岗位和工作地点。具体填报口径请咨询所在院系。', 2, 1, 0, 3, '测试管理员', 0, '2026-10-24 06:00:00.000', '2026-10-24 06:00:00.000', '2026-10-24 06:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (5, '【测试】毕业去向变更申请说明', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n已归档备案如需更新，可创建变更申请并填写原因。新申请重新进入审核流程，原记录保留。', 3, 1, 0, 3, '测试管理员', 0, '2026-10-24 05:00:00.000', '2026-10-24 05:00:00.000', '2026-10-24 05:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (6, '【测试】审核中申请提交规则', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n已有待初审或待复审备案时，不能提交新的申请。请在就业备案页面查看最新进度。', 3, 1, 0, 3, '测试管理员', 0, '2026-10-24 04:00:00.000', '2026-10-24 04:00:00.000', '2026-10-24 04:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (7, '【测试】求职信息核验提示', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n求职前请核实招聘单位与岗位信息，通过可靠渠道了解招聘流程。', 2, 1, 0, 3, '测试管理员', 0, '2026-10-24 03:00:00.000', '2026-10-24 03:00:00.000', '2026-10-24 03:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (8, '【测试】校园招聘信息关注提醒', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请关注学校后续发布的招聘安排。本文为展示示例，不代表实际招聘活动。', 4, 1, 0, 3, '测试管理员', 0, '2026-10-24 02:00:00.000', '2026-10-24 02:00:00.000', '2026-10-24 02:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (9, '【测试】班级与专业资料核对', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请核对个人中心的学院、专业与班级信息。发现不一致时，请联系负责老师处理。', 1, 1, 0, 3, '测试管理员', 0, '2026-10-24 01:00:00.000', '2026-10-24 01:00:00.000', '2026-10-24 01:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (10, '【测试】备案审核进度查看说明', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n学生可以在就业备案页面查看待辅导员初审、待院系复审、已归档或已打回状态。', 3, 1, 0, 3, '测试管理员', 0, '2026-10-24 00:00:00.000', '2026-10-24 00:00:00.000', '2026-10-24 00:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (11, '【测试】就业指导咨询提示', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n如对毕业去向填写有疑问，请向所在院系或辅导员咨询。具体咨询安排以正式通知为准。', 4, 1, 0, 3, '测试管理员', 0, '2026-10-23 23:00:00.000', '2026-10-23 23:00:00.000', '2026-10-23 23:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (12, '【测试】账号登录与密码维护提醒', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n请妥善保管登录账号和密码，不向他人提供登录凭据。首次登录后请及时维护个人密码。', 1, 1, 0, 3, '测试管理员', 0, '2026-10-23 22:00:00.000', '2026-10-23 22:00:00.000', '2026-10-23 22:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (13, '【测试】毕业生材料核验安排草稿', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n此公告尚在编辑，仅用于验证草稿不向学生展示。', 3, 0, 0, 3, '测试管理员', 0, NULL, '2026-10-23 21:00:00.000', '2026-10-23 21:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (14, '【测试】就业指导活动安排草稿', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n活动时间与地点尚未确定，仅用于验证公告编辑和分页。', 4, 0, 0, 3, '测试管理员', 0, NULL, '2026-10-23 20:00:00.000', '2026-10-23 20:00:00.000');
INSERT INTO `sys_notice` (`id`, `title`, `content`, `notice_type`, `status`, `is_top`, `publisher_id`, `publisher_name`, `view_count`, `publish_time`, `created_at`, `updated_at`) VALUES (15, '【测试】已撤回的备案安排示例', '【测试数据，仅用于系统演示，不作为学校正式通知】\n\n该公告已撤回，仅用于验证学生不能查看撤回内容。', 3, 2, 0, 3, '测试管理员', 0, '2026-10-23 19:00:00.000', '2026-10-23 19:00:00.000', '2026-10-23 19:00:00.000');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码，如 STUDENT、TEACHER、ADMIN',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值，越小越靠前',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RBAC角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `sort_order`, `remark`, `created_at`, `updated_at`) VALUES (1, '管理员', 'ADMIN', 1, 10, '系统管理', '2026-10-18 14:00:22.272', '2026-10-18 14:00:22.272');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `sort_order`, `remark`, `created_at`, `updated_at`) VALUES (2, '院系负责人', 'DEPARTMENT_HEAD', 1, 20, '院系管理与审核', '2026-10-18 14:00:22.272', '2026-10-18 14:00:22.272');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `sort_order`, `remark`, `created_at`, `updated_at`) VALUES (3, '辅导员', 'COUNSELOR', 1, 30, '学生管理与就业备案审核', '2026-10-18 14:00:22.272', '2026-10-18 14:00:22.272');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `sort_order`, `remark`, `created_at`, `updated_at`) VALUES (4, '学生', 'STUDENT', 1, 40, '个人资料与就业备案申请', '2026-10-18 14:00:22.272', '2026-10-18 14:00:22.272');
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `role_id` bigint unsigned NOT NULL,
  `menu_id` bigint unsigned NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `menu_id` (`menu_id`),
  CONSTRAINT `sys_role_menu_ibfk_1` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE,
  CONSTRAINT `sys_role_menu_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表\n';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (1, 1, 1, '2026-10-26 13:15:35.509');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (2, 3, 1, '2026-10-26 13:15:35.509');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (3, 2, 1, '2026-10-26 13:15:35.509');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (4, 1, 2, '2026-10-26 13:15:35.510');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (5, 3, 2, '2026-10-26 13:15:35.510');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (6, 2, 2, '2026-10-26 13:15:35.510');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (7, 3, 3, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (8, 2, 3, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (10, 1, 4, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (11, 3, 4, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (12, 2, 4, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (13, 1, 5, '2026-10-26 13:15:35.511');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (14, 1, 6, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (15, 2, 6, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (17, 1, 7, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (18, 1, 8, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (19, 3, 8, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (20, 2, 8, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (21, 1, 9, '2026-10-26 13:15:35.512');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (22, 1, 10, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (23, 3, 11, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (24, 2, 11, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (26, 1, 12, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (27, 1, 13, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (28, 1, 14, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (29, 1, 15, '2026-10-26 13:15:35.513');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (30, 1, 16, '2026-10-26 13:15:35.514');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (31, 4, 17, '2026-10-26 13:15:35.514');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (32, 4, 18, '2026-10-26 13:15:35.514');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (33, 4, 19, '2026-10-26 13:15:35.514');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (34, 4, 20, '2026-10-26 13:15:35.514');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (35, 4, 21, '2026-10-26 13:15:35.515');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (36, 4, 22, '2026-10-26 13:15:35.515');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (37, 4, 23, '2026-10-26 13:15:35.515');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (38, 1, 24, '2026-10-26 13:42:56.381');
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(255) NOT NULL COMMENT '密码哈希',
  `counselor_id` bigint DEFAULT NULL COMMENT '关联辅导员ID',
  `real_name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1正常',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `student_uuid` bigint DEFAULT NULL COMMENT '关联student.uuid',
  `account_type` varchar(16) NOT NULL DEFAULT 'STAFF' COMMENT 'STUDENT学生 STAFF教职工',
  `password_algorithm` varchar(10) NOT NULL DEFAULT 'BCRYPT' COMMENT 'BCRYPT或迁移期间MD5',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_counselor_id` (`counselor_id`),
  UNIQUE KEY `uk_sys_user_student` (`student_uuid`),
  CONSTRAINT `fk_sys_user_student` FOREIGN KEY (`student_uuid`) REFERENCES `student` (`uuid`),
  CONSTRAINT `chk_sys_user_account_type` CHECK ((((`account_type` = _utf8mb4'STUDENT') and (`student_uuid` is not null) and (`counselor_id` is null)) or ((`account_type` = _utf8mb4'STAFF') and (`student_uuid` is null)))),
  CONSTRAINT `chk_sys_user_password_algorithm` CHECK ((`password_algorithm` in (_utf8mb4'BCRYPT',_utf8mb4'MD5')))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教职工及管理员账号表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (1, 'test_teacher01', '$2y$10$23rlj5UkR0SrucYz.nXy1.skFc3ZOdJm9BD/n4O/SsANyEw6vfSQq', 1, '测试教师一1', 1, '2026-10-16 10:54:06.898', '2026-10-18 15:18:37.621', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (2, 'test_teacher02', '$2y$10$FEK2Ugks9BfaJoO2rl9DteHZNNrX2HlQRLWveZ4rs3WAylTloeyr.', 2, '测试教师二', 1, '2026-10-16 10:54:06.906', '2026-10-18 15:18:38.343', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (3, 'test_admin01', '$2y$10$n82jnNTJ6EVrn2Amz73DSukFVX3mLNEEZPPMYYUMdGgEHacUEX3cK', 3, '测试管理员', 1, '2026-10-16 10:54:06.907', '2026-10-18 15:18:39.076', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (4, '2026001001', '49ad2fb6f1f8247230413dc975efd510', NULL, '张三', 1, '2026-10-18 12:26:23.475', '2026-10-18 12:26:23.475', 1, 'STUDENT', 'MD5');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (5, '2026001002', 'd763c394f6a9c868ce44dcf4ddd253f0', NULL, '李四', 1, '2026-10-18 12:26:23.475', '2026-10-18 12:26:23.475', 2, 'STUDENT', 'MD5');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (6, '25230820025', '$2a$10$T3zBVWRzw3B4hvLm/AtnYelcs0aKhzuwmVsKYQuRO.CHM5wW4VNXS', NULL, 'james', 1, '2026-10-18 12:26:23.475', '2026-10-18 12:26:44.881', 3, 'STUDENT', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (7, '20260111', '$2a$10$0wxixIcVOtXK0W7YsLrlD.gqUGwFrcvWMiMQqQTyu7cNw.fl7vQBK', 4, '叶B', 1, '2026-10-18 15:17:36.624', '2026-10-18 15:17:36.624', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (8, '2026test1', '$2a$10$q78uBEodJx8FG7By2YiZ3eE57yQQviFxg9lTr1WfPYpfzPrtvTaoq', NULL, '叶鹏', 1, '2026-10-18 16:15:01.174', '2026-10-18 16:15:01.174', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (9, '25230820026', '$2a$10$zsCvd/NOIi1CzDv.Fz10/Oancb/Ob1wkmH.2SKaCSQaEXyLb1gsxS', NULL, 'rony', 1, '2026-10-23 15:06:02.835', '2026-10-23 15:06:02.835', 4, 'STUDENT', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (10, '25230820021', '$2a$10$46nHzv2.bQvXum3GykLmauxzaHSHeciFgyQTxrwrdrmlEfnfTMUyK', NULL, 'J2', 1, '2026-10-23 15:24:26.407', '2026-10-23 15:24:26.407', 5, 'STUDENT', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (11, 'jiahaoTest', '$2a$10$3SrvsNzoLeHcbE42mmjONeoGdEA0di3c.ggMTtljzRP1FbsVbqrra', NULL, '张三', 1, '2026-10-23 21:29:58.903', '2026-10-23 21:29:58.903', NULL, 'STAFF', 'BCRYPT');
INSERT INTO `sys_user` (`id`, `username`, `password`, `counselor_id`, `real_name`, `status`, `created_at`, `updated_at`, `student_uuid`, `account_type`, `password_algorithm`) VALUES (12, 'system_admin01', '$2y$10$Xil8WMmxNc8xGoc0Hw8RseR9qR851PbXw76XWUK7crEFUeAOWAtLa', NULL, '系统管理员', 1, '2026-10-26 13:19:08.579', '2026-10-26 13:19:08.579', NULL, 'STAFF', 'BCRYPT');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '绑定记录ID',
  `user_id` bigint unsigned NOT NULL COMMENT '账号ID，关联sys_user.id',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID，关联sys_role.id',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '绑定时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`,`role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RBAC账号角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (1, 4, 4, '2026-10-18 14:00:22.282');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (2, 5, 4, '2026-10-18 14:00:22.282');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (3, 6, 4, '2026-10-18 14:00:22.282');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (4, 1, 3, '2026-10-18 15:21:17.746');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (5, 2, 3, '2026-10-18 15:21:17.746');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (6, 3, 3, '2026-10-18 15:21:17.746');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (7, 7, 3, '2026-10-18 15:21:17.746');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (11, 8, 2, '2026-10-18 16:15:01.191');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (12, 9, 4, '2026-10-23 15:06:02.840');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (13, 10, 4, '2026-10-23 15:24:26.411');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (14, 11, 2, '2026-10-23 21:29:58.912');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (15, 12, 1, '2026-10-26 13:19:08.580');
COMMIT;

-- ----------------------------
-- Table structure for work_compare
-- ----------------------------
DROP TABLE IF EXISTS `work_compare`;
CREATE TABLE `work_compare` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `education` varchar(30) NOT NULL,
  `major` varchar(100) DEFAULT NULL,
  `experience` int NOT NULL,
  `skills` json NOT NULL,
  `categories` json NOT NULL,
  `cities` json NOT NULL,
  `job_type` varchar(20) NOT NULL,
  `minimum_salary` decimal(10,2) DEFAULT NULL,
  `expected_salary` decimal(10,2) DEFAULT NULL,
  `work_mode` varchar(20) DEFAULT NULL,
  `rest_days` varchar(20) DEFAULT NULL,
  `travel` varchar(30) DEFAULT NULL,
  `outsourcing` varchar(20) DEFAULT NULL,
  `priority` varchar(30) NOT NULL,
  `other` varchar(1000) DEFAULT NULL,
  `strict` json DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `company_contact` varchar(200) DEFAULT NULL COMMENT '公司电话或邮箱',
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_work_compare_experience` CHECK ((`experience` between 0 and 60)),
  CONSTRAINT `ck_work_compare_salary` CHECK ((((`minimum_salary` is null) or (`minimum_salary` >= 0)) and ((`expected_salary` is null) or (`expected_salary` >= 0)) and ((`minimum_salary` is null) or (`expected_salary` is null) or (`expected_salary` >= `minimum_salary`))))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='求职条件对比记录';

-- ----------------------------
-- Records of work_compare
-- ----------------------------
BEGIN;
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (1, '本科', '软件工程', 0, '[\"Java\", \"Spring Boot\", \"MySQL\"]', '[\"后端开发\"]', '[\"杭州\"]', '全职', 6000.00, 9000.00, '现场办公', '双休', '不接受', '不接受', '技能匹配', '【测试】对比样例 01，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.657', '2026-10-24 17:10:38.331', '【测试】杭州云程科技有限公司', 'jobs01@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (2, '本科', '计算机科学与技术', 1, '[\"JavaScript\", \"Vue\"]', '[\"前端开发\"]', '[\"上海\", \"杭州\"]', '全职', 8000.00, 12000.00, '混合办公', '双休', '接受偶尔出差', '不限', '薪资', '【测试】对比样例 02，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.664', '2026-10-24 17:10:38.335', '【测试】沪杭数字科技有限公司', 'jobs02@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (3, '大专', NULL, 0, '[\"Excel\", \"沟通协作\"]', '[\"行政人事\"]', '[\"温州\"]', '全职', 4000.00, 5500.00, '现场办公', '单双休', '不接受', '接受', '地点', '【测试】对比样例 03，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.665', '2026-10-24 17:10:38.335', '【测试】温州启航服务有限公司', 'jobs03@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (4, '硕士', '统计学', 0, '[\"Python\", \"数据分析\"]', '[\"数据分析\"]', '[\"不限\"]', '实习', NULL, 4000.00, NULL, NULL, '不限', '不限', '发展空间', '【测试】对比样例 04，仅供开发验证，非真实岗位。', '{\"cities\": false, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.665', '2026-10-24 17:10:38.336', '【测试】数研信息科技有限公司', 'jobs04@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (5, '本科', '视觉传达设计', 2, '[\"设计\"]', '[\"设计\"]', '[\"深圳\"]', '全职', 7000.00, 11000.00, '远程', '双休', '不接受', '不接受', '工作方式', '【测试】对比样例 05，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.665', '2026-10-24 17:10:38.336', '【测试】深圳创意设计有限公司', 'jobs05@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (6, '本科', '财务管理', 0, '[\"Excel\"]', '[\"财务\"]', '[\"宁波\"]', '全职', 4500.00, 6500.00, '现场办公', '双休', '接受偶尔出差', '不限', '专业相关性', '【测试】对比样例 06，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.665', '2026-10-24 17:10:38.337', '【测试】宁波汇诚财务有限公司', 'jobs06@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (7, '大专', NULL, 3, '[\"沟通协作\"]', '[\"市场营销\"]', '[\"广州\"]', '兼职', NULL, NULL, NULL, '单休', '不限', '接受', '薪资', '【测试】对比样例 07，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.666', '2026-10-24 17:10:38.337', '【测试】广州远行营销有限公司', 'jobs07@example.com');
INSERT INTO `work_compare` (`id`, `education`, `major`, `experience`, `skills`, `categories`, `cities`, `job_type`, `minimum_salary`, `expected_salary`, `work_mode`, `rest_days`, `travel`, `outsourcing`, `priority`, `other`, `strict`, `created_at`, `updated_at`, `company_name`, `company_contact`) VALUES (8, '本科', '计算机科学与技术', 0, '[\"Java\", \"MySQL\"]', '[\"软件测试\", \"后端开发\"]', '[\"成都\", \"武汉\"]', '实习', 2500.00, 4000.00, '现场办公', NULL, NULL, NULL, '技能匹配', '【测试】对比样例 08，仅供开发验证，非真实岗位。', '{\"cities\": true, \"travel\": false, \"restDays\": false, \"workMode\": false, \"outsourcing\": false}', '2026-10-24 14:31:33.666', '2026-10-24 17:10:38.337', '【测试】蓉汉软件科技有限公司', 'jobs08@example.com');
COMMIT;

-- ----------------------------
-- Table structure for work_skill
-- ----------------------------
DROP TABLE IF EXISTS `work_skill`;
CREATE TABLE `work_skill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_skill_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作技能字典';

-- ----------------------------
-- Records of work_skill
-- ----------------------------
BEGIN;
INSERT INTO `work_skill` (`id`, `name`) VALUES (27, 'C++');
INSERT INTO `work_skill` (`id`, `name`) VALUES (13, 'CSS');
INSERT INTO `work_skill` (`id`, `name`) VALUES (17, 'Docker');
INSERT INTO `work_skill` (`id`, `name`) VALUES (8, 'Excel');
INSERT INTO `work_skill` (`id`, `name`) VALUES (23, 'Figma');
INSERT INTO `work_skill` (`id`, `name`) VALUES (15, 'Git');
INSERT INTO `work_skill` (`id`, `name`) VALUES (28, 'Go');
INSERT INTO `work_skill` (`id`, `name`) VALUES (12, 'HTML');
INSERT INTO `work_skill` (`id`, `name`) VALUES (1, 'Java');
INSERT INTO `work_skill` (`id`, `name`) VALUES (4, 'JavaScript');
INSERT INTO `work_skill` (`id`, `name`) VALUES (30, 'Kubernetes');
INSERT INTO `work_skill` (`id`, `name`) VALUES (16, 'Linux');
INSERT INTO `work_skill` (`id`, `name`) VALUES (3, 'MySQL');
INSERT INTO `work_skill` (`id`, `name`) VALUES (29, 'Node.js');
INSERT INTO `work_skill` (`id`, `name`) VALUES (22, 'Photoshop');
INSERT INTO `work_skill` (`id`, `name`) VALUES (7, 'Python');
INSERT INTO `work_skill` (`id`, `name`) VALUES (6, 'React');
INSERT INTO `work_skill` (`id`, `name`) VALUES (18, 'Redis');
INSERT INTO `work_skill` (`id`, `name`) VALUES (2, 'Spring Boot');
INSERT INTO `work_skill` (`id`, `name`) VALUES (19, 'Spring Cloud');
INSERT INTO `work_skill` (`id`, `name`) VALUES (20, 'SQL');
INSERT INTO `work_skill` (`id`, `name`) VALUES (14, 'TypeScript');
INSERT INTO `work_skill` (`id`, `name`) VALUES (5, 'Vue');
INSERT INTO `work_skill` (`id`, `name`) VALUES (25, '市场营销');
INSERT INTO `work_skill` (`id`, `name`) VALUES (9, '数据分析');
INSERT INTO `work_skill` (`id`, `name`) VALUES (10, '沟通协作');
INSERT INTO `work_skill` (`id`, `name`) VALUES (11, '设计');
INSERT INTO `work_skill` (`id`, `name`) VALUES (24, '财务分析');
INSERT INTO `work_skill` (`id`, `name`) VALUES (21, '软件测试');
INSERT INTO `work_skill` (`id`, `name`) VALUES (26, '项目管理');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
