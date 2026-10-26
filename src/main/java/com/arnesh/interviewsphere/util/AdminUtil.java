package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.entity.LeetCodeQuestion;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.LeetCodeQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminUtil {

    private final LeetCodeQuestionRepository leetCodeQuestionRepository;

    
    public List<Integer> generateCodingIds(String company, int codingQuestionCount) {
        long totalCount = leetCodeQuestionRepository.countByCompaniesContaining(company);

        if (totalCount < codingQuestionCount) {
            throw new CustomException("Not enough coding questions, only " + totalCount + " available.",
                    HttpStatus.BAD_REQUEST);
        }

        List<LeetCodeQuestion> easyQuestions = leetCodeQuestionRepository.findRandomByCompanyAndDifficulty(company,
                "Easy",
                codingQuestionCount);
        List<LeetCodeQuestion> mediumQuestions = leetCodeQuestionRepository.findRandomByCompanyAndDifficulty(company,
                "Medium",
                codingQuestionCount);
        List<LeetCodeQuestion> hardQuestions = leetCodeQuestionRepository.findRandomByCompanyAndDifficulty(company,
                "Hard",
                codingQuestionCount);

        List<LeetCodeQuestion> codingPool = new ArrayList<>();
        codingPool.addAll(easyQuestions);
        codingPool.addAll(mediumQuestions);
        codingPool.addAll(hardQuestions);

        Collections.shuffle(codingPool);

        return codingPool.stream()
                .limit(codingQuestionCount)
                .map(q -> q.getQuestionId())
                .toList();
    }
}