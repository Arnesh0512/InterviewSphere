package com.arnesh.interviewsphere.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Difficulty;
import com.arnesh.interviewsphere.constant.Language;
import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;
import com.arnesh.interviewsphere.constant.Tag;
import com.arnesh.interviewsphere.constant.Topic;
import com.arnesh.interviewsphere.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/constants")
public class ConstantController {

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCompanies() {
        List<String> companies = Arrays.stream(Company.values()).map(c -> c.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("companies", companies)));
    }

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopics() {
        List<String> topics = Arrays.stream(Topic.values()).map(t -> t.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("topics", topics)));
    }

    @GetMapping("/difficulties")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDifficulties() {
        List<String> difficulties = Arrays.stream(Difficulty.values()).map(d -> d.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("difficulties", difficulties)));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLanguages() {
        List<String> languages = Arrays.stream(Language.values()).map(l -> l.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("languages", languages)));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoles() {
        List<String> roles = Arrays.stream(Role.values()).map(r -> r.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("roles", roles)));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSkills() {
        List<String> skills = Arrays.stream(Skill.values()).map(s -> s.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("skills", skills)));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTags() {
        List<String> tags = Arrays.stream(Tag.values()).map(t -> t.getValue()).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("tags", tags)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllConstants() {
        Map<String, Object> allConstants = Map.of(
                "companies", Arrays.stream(Company.values()).map(c -> c.getValue()).toList(),
                "topics", Arrays.stream(Topic.values()).map(t -> t.getValue()).toList(),
                "difficulties", Arrays.stream(Difficulty.values()).map(d -> d.getValue()).toList(),
                "languages", Arrays.stream(Language.values()).map(l -> l.getValue()).toList(),
                "roles", Arrays.stream(Role.values()).map(r -> r.getValue()).toList(),
                "skills", Arrays.stream(Skill.values()).map(s -> s.getValue()).toList(),
                "tags", Arrays.stream(Tag.values()).map(t -> t.getValue()).toList()
        );
        return ResponseEntity.ok(ApiResponse.success(allConstants));
    }
}