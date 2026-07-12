package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.service.impl.BloodTestService;
import com.dragonraja.forum.vo.BloodTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-test")
@RequiredArgsConstructor
public class BloodTestController {

    private final BloodTestService bloodTestService;

    @GetMapping("/exam")
    public Result<List<Map<String, Object>>> getExam() {
        return Result.success(bloodTestService.getExamQuestions());
    }

    @PostMapping("/submit")
    public Result<BloodTestResult> submitExam(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("answers")) {
            return Result.error(400, "请提交答题结果");
        }
        @SuppressWarnings("unchecked")
        Map<Long, String> answers = new java.util.LinkedHashMap<>();
        Object answersObj = body.get("answers");
        if (answersObj instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) answersObj).entrySet()) {
                answers.put(Long.valueOf(e.getKey().toString()), e.getValue().toString());
            }
        }
        String bloodType = body.get("bloodType") != null ? body.get("bloodType").toString() : null;
        return Result.success(bloodTestService.submitExam(answers, bloodType));
    }

    /** 跳过测试，随机分配血统和言灵 */
    @PostMapping("/skip")
    public Result<BloodTestResult> skipTest(@RequestBody(required = false) Map<String, Object> body) {
        String bloodType = body != null && body.get("bloodType") != null ? body.get("bloodType").toString() : null;
        return Result.success(bloodTestService.randomResult(bloodType));
    }
}
