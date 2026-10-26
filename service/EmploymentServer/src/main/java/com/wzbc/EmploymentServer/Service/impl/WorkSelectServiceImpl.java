package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Mapper.WorkCompareMapper;
import com.wzbc.EmploymentServer.Service.WorkSelectService;
import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSelectDTO;
import com.wzbc.EmploymentServer.entity.VO.WorkSelect.WorkRecommendationVO;
import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class WorkSelectServiceImpl implements WorkSelectService
{
    @Autowired
    private WorkCompareMapper workCompareMapper;
    @Override
    public List<WorkRecommendationVO> getRecommendedJobs(WorkSelectDTO dto)
    {
        return workCompareMapper.selectAll()
                .stream()
                .filter(job -> meetsRequiredConditions(dto, job))
                .map(job ->
                {
                    WorkRecommendationVO result = CopyTools.copy(job, WorkRecommendationVO.class);
                    result.setSimilarity(calculateSimilarity(dto, job));
                    if (job.getStrict() != null)
                        result.setStrict(CopyTools.copy(job.getStrict(), WorkRecommendationVO.StrictConditions.class));
                    return result;
                })
                .sorted(Comparator.comparingDouble(WorkRecommendationVO::getSimilarity)
                                .reversed()
                                .thenComparing(WorkRecommendationVO::getId))
                .toList();
    }
    /** null 表示用户未表达偏好，不参与分母；岗位缺失信息不能视为满足。 */
    public double calculateSimilarity(WorkSelectDTO u, WorkComparePO j)
    {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("学历", judgeEducation(u.getEducation(), j.getEducation()));
        scores.put("专业相关性", judgeMajor(u.getMajor(), j.getMajor()));
        scores.put("年限", judgeExperience(u.getExperience(), j.getExperience()));
        scores.put("技能匹配", judgeSkills(u.getSkills(), j.getSkills()));
        scores.put("岗位类别", judgeCategories(u.getCategories(), j.getCategories()));
        scores.put("地点", judgeCities(u.getCities(), j.getCities()));
        scores.put("求职类型", judgeJobType(u.getJobType(), j.getJobType()));
        scores.put("最低薪资", judgeMinimumSalary(u.getMinimumSalary(), j.getMinimumSalary()));
        scores.put("薪资", judgeExpectedSalary(u.getExpectedSalary(), j.getExpectedSalary()));
        scores.put("工作方式", judgeWorkMode(u.getWorkMode(), j.getWorkMode()));
        scores.put("休息制度", judgeRestDays(u.getRestDays(), j.getRestDays()));
        scores.put("出差", judgeTravel(u.getTravel(), j.getTravel()));
        scores.put("外包", judgeOutsourcing(u.getOutsourcing(), j.getOutsourcing()));
        scores.put("补充说明", judgeOther(u.getOther(), j.getOther()));
        // priority 仅决定权重；other 为展示文本，不做字符串相等评分。
        double sum = 0, weights = 0;
        for (var item : scores.entrySet())
            if (item.getValue() != null)
            {
                double weight = judgePriority(u.getPriority(), item.getKey());
                sum += item.getValue() * weight;
                weights += weight;
            }
        return weights == 0 ? 0 : Math.round(sum / weights * 10000) / 100.0;
    }
    private static String text(String s)
    {
        return s == null || s.isBlank() ? null : s.strip();
    }
    private static boolean any(String s)
    {
        return "不限".equals(text(s));
    }
    private static Set<String> values(List<String> a)
    {
        Set<String> s = new HashSet<>();
        if (a != null)
            for (String v : a)
                if (text(v) != null)
                    s.add(text(v));
        return s;
    }
    private Double exact(String user, String job)
    {
        if (text(user) == null || any(user))
            return null;
        return Objects.equals(text(user), text(job)) || any(job) ? 1.0 : 0.0;
    }
    public Double judgeEducation(String user, String job)
    {
        if (text(user) == null)
            return null;
        if (any(job))
            return 1.0;
        List<String> ranks = List.of("高中及以下", "中专 / 职高", "大专", "本科", "硕士", "博士");
        String u = "专科".equals(user) ? "大专" : text(user), j = "专科".equals(job) ? "大专" : text(job);
        int a = u == null ? -1 : ranks.indexOf(u), b = j == null ? -1 : ranks.indexOf(j);
        return a >= 0 && b >= 0 && a >= b ? 1.0 : 0.0;
    }
    public Double judgeMajor(String u, String j)
    {
        return exact(u, j);
    }
    public Double judgeExperience(Integer u, Integer j)
    {
        return u == null ? null : j != null && u >= j ? 1.0 : 0.0;
    }
    public Double judgeSkills(List<String> u, List<String> j)
    {
        Set<String> a = values(u), b = values(j);
        if (a.isEmpty())
            return null;
        if (b.isEmpty())
            return 0.0;
        int count = b.size();
        b.retainAll(a);
        return (double)b.size() / count;
    }
    private Double overlaps(List<String> u, List<String> j)
    {
        Set<String> a = values(u), b = values(j);
        if (a.isEmpty() || a.contains("不限"))
            return null;
        if (b.contains("不限"))
            return 1.0;
        a.retainAll(b);
        return a.isEmpty() ? 0.0 : 1.0;
    }
    public Double judgeCategories(List<String> u, List<String> j)
    {
        return overlaps(u, j);
    }
    public Double judgeCities(List<String> u, List<String> j)
    {
        return overlaps(u, j);
    }
    public Double judgeJobType(String u, String j)
    {
        return exact(u, j);
    }
    public Double judgeMinimumSalary(BigDecimal u, BigDecimal j)
    {
        return u == null ? null : j != null && j.compareTo(u) >= 0 ? 1.0 : 0.0;
    }
    public Double judgeExpectedSalary(BigDecimal u, BigDecimal j)
    {
        if (u == null)
            return null;
        if (j == null)
            return 0.0;
        if (j.compareTo(u) >= 0)
            return 1.0;
        return Math.max(0, j.divide(u, MathContext.DECIMAL64).doubleValue());
    }
    public Double judgeWorkMode(String u, String j)
    {
        return exact(u, j);
    }
    public Double judgeRestDays(String u, String j)
    {
        if (text(u) == null || any(u))
            return null;
        List<String> rank = List.of("单休", "单双休", "双休");
        int a = rank.indexOf(text(u)), b = text(j) == null ? -1 : rank.indexOf(text(j));
        return a >= 0 && b >= a ? 1.0 : 0.0;
    }
    public Double judgeTravel(String u, String j)
    {
        if (text(u) == null || any(u))
            return null;
        if ("接受偶尔出差".equals(u) && "不接受".equals(j))
            return 1.0;
        return Objects.equals(text(u), text(j)) ? 1.0 : 0.0;
    }
    public Double judgeOutsourcing(String u, String j)
    {
        if (text(u) == null || any(u))
            return null;
        if ("接受".equals(u) && ("接受".equals(j) || "不接受".equals(j)))
            return 1.0;
        return Objects.equals(text(u), text(j)) ? 1.0 : 0.0;
    }
    public Double judgeOther(String user, String job)
    {
        return null;
    } // 自由文本仅展示，不推断语义。
    public double judgePriority(String priority, String field)
    {
        return Objects.equals(priority, field) ? 2.0 : 1.0;
    }
    public boolean meetsRequiredConditions(WorkSelectDTO u, WorkComparePO j)
    {
        if (u.getMinimumSalary() != null &&
            !Double.valueOf(1).equals(judgeMinimumSalary(u.getMinimumSalary(), j.getMinimumSalary())))
            return false;
        var s = u.getStrict();
        if (s == null)
            return true;
        return required(s.getCities(), judgeCities(u.getCities(), j.getCities())) &&
                required(s.getWorkMode(), judgeWorkMode(u.getWorkMode(), j.getWorkMode())) &&
                required(s.getRestDays(), judgeRestDays(u.getRestDays(), j.getRestDays())) &&
                required(s.getTravel(), judgeTravel(u.getTravel(), j.getTravel())) &&
                required(s.getOutsourcing(), judgeOutsourcing(u.getOutsourcing(), j.getOutsourcing()));
    }
    private boolean required(Boolean strict, Double score)
    {
        return !Boolean.TRUE.equals(strict) || Double.valueOf(1).equals(score);
    }
}
