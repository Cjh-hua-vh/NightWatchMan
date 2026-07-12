package com.dragonraja.forum.service.impl;

import com.dragonraja.forum.entity.Question;
import com.dragonraja.forum.mapper.QuestionMapper;
import com.dragonraja.forum.vo.BloodTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BloodTestService {

    private final QuestionMapper questionMapper;

    /** 言灵池 */
    private static final Map<String, List<String[]>> YANLING_POOL = new LinkedHashMap<>();
    static {
        // grade -> [(言灵名, 序列号), ...]
        YANLING_POOL.put("S", List.of(
            new String[]{"言灵·皇帝", "序列号001"},
            new String[]{"言灵·王权", "序列号003"},
            new String[]{"言灵·天罚", "序列号007"},
            new String[]{"言灵·审判", "序列号009"},
            new String[]{"言灵·永恒", "序列号012"}
        ));
        YANLING_POOL.put("A", List.of(
            new String[]{"言灵·镰鼬", "序列号021"},
            new String[]{"言灵·时间零", "序列号023"},
            new String[]{"言灵·不朽", "序列号028"},
            new String[]{"言灵·黑日", "序列号031"},
            new String[]{"言灵·因陀罗", "序列号035"}
        ));
        YANLING_POOL.put("B", List.of(
            new String[]{"言灵·雷池", "序列号052"},
            new String[]{"言灵·炽日", "序列号055"},
            new String[]{"言灵·阴雷", "序列号058"},
            new String[]{"言灵·刹那", "序列号061"},
            new String[]{"言灵·风魔", "序列号064"}
        ));
        YANLING_POOL.put("C", List.of(
            new String[]{"言灵·无尘之地", "序列号072"},
            new String[]{"言灵·青铜御座", "序列号075"},
            new String[]{"言灵·君焰", "序列号078"},
            new String[]{"言灵·风王之瞳", "序列号082"}
        ));
        YANLING_POOL.put("D", List.of(
            new String[]{"言灵·影", "序列号095"},
            new String[]{"言灵·风", "序列号098"},
            new String[]{"言灵·火", "序列号101"},
            new String[]{"言灵·尘", "序列号105"},
            new String[]{"言灵·结界", "序列号108"}
        ));
    }

    /** 获取9道随机题 + 1道必考题(血之哀) + 1道血型题 = 共11题 */
    public List<Map<String, Object>> getExamQuestions() {
        // 先获取血型题，用于排除和最后添加
        Question bloodQ = questionMapper.selectByQuestion("你的血型是什么？");
        Long bloodQId = bloodQ != null ? bloodQ.getId() : null;
        
        // 9道随机题（排除血之哀和血型题避免重复）
        List<Question> pool = questionMapper.selectRandomExclude(9, 49L, bloodQId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Question q : pool) {
            result.add(toQuestionMap(q));
        }
        // 必考题：血之哀
        Question bloodSorrow = questionMapper.selectById(49L);
        if (bloodSorrow != null) {
            Map<String, Object> m = toQuestionMap(bloodSorrow);
            m.put("mandatory", true);
            result.add(m);
        }
        // 最后一题固定为血型题
        if (bloodQ != null) {
            Map<String, Object> m = toQuestionMap(bloodQ);
            m.put("isBloodType", true);
            result.add(m);
        }
        return result;
    }

    private Map<String, Object> toQuestionMap(Question q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("category", q.getCategory());
        m.put("question", q.getQuestion());
        m.put("optionA", q.getOptionA());
        m.put("optionB", q.getOptionB());
        m.put("optionC", q.getOptionC());
        m.put("optionD", q.getOptionD());
        return m;
    }

    /** 判卷并返回血统结果（answers不含血型题，bloodType单独传入） */
    public BloodTestResult submitExam(Map<Long, String> answers, String bloodType) {
        int correct = 0;
        for (Map.Entry<Long, String> e : answers.entrySet()) {
            Question q = questionMapper.selectById(e.getKey());
            if (q != null && q.getAnswer().equalsIgnoreCase(e.getValue())) {
                correct++;
            }
        }
        int total = 10; // 固定10道计分题
        double rate = total == 0 ? 0 : (double) correct / total;

        String grade;
        if (rate >= 0.9) grade = "S";
        else if (rate >= 0.7) grade = "A";
        else if (rate >= 0.5) grade = "B";
        else if (rate >= 0.3) grade = "C";
        else grade = "D";

        Random rand = new Random();
        List<String[]> pool = YANLING_POOL.get(grade);
        String[] picked = pool.get(rand.nextInt(pool.size()));

        BloodTestResult r = new BloodTestResult();
        r.setGrade(grade);
        r.setCorrectCount(correct);
        r.setTotalCount(total);
        r.setRate((int)(rate * 100));
        r.setYanling(picked[0]);
        r.setSequence(picked[1]);
        r.setBloodType(bloodType);
        return r;
    }

    /** 跳过测试，随机分配血统和言灵 */
    public BloodTestResult randomResult(String bloodType) {
        Random rand = new Random();
        // 按权重随机血统等级（S 5%, A 15%, B 35%, C 30%, D 15%）
        String[] grades = {"S","A","B","C","D"};
        int[] weights = {5, 15, 35, 30, 15};
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;
        int roll = rand.nextInt(totalWeight);
        String grade = "D";
        int cumulative = 0;
        for (int i = 0; i < grades.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) { grade = grades[i]; break; }
        }

        List<String[]> pool = YANLING_POOL.get(grade);
        String[] picked = pool.get(rand.nextInt(pool.size()));

        BloodTestResult r = new BloodTestResult();
        r.setGrade(grade);
        r.setCorrectCount(0);
        r.setTotalCount(0);
        r.setRate(0);
        r.setYanling(picked[0]);
        r.setSequence(picked[1]);
        r.setBloodType(bloodType);
        return r;
    }
}
