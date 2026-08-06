package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.LeetCodeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;

@Repository
public interface LeetCodeQuestionRepository extends JpaRepository<LeetCodeQuestion, Integer> {

    List<LeetCodeQuestion> findByQuestionIdIn(List<Integer> questionIds);

    @Query(value = "SELECT COUNT(*) FROM leetcode_questions WHERE :company = ANY(companies)", nativeQuery = true)
    long countByCompaniesContaining(@Param("company") String company);

    @Query(value = """
        SELECT * FROM leetcode_questions
        WHERE :company = ANY(companies) AND difficulty = :difficulty
        ORDER BY RANDOM() LIMIT :limit
        """, nativeQuery = true)
    List<LeetCodeQuestion> findRandomByCompanyAndDifficulty(
            @Param("company") String company,
            @Param("difficulty") String difficulty,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT COUNT(*) FROM leetcode_questions q
        WHERE (:hasCompanies = false OR q.companies && CAST(:companies AS text[]))
          AND (:hasTags = false OR q.tags && CAST(:tags AS text[]))
          AND (:hasDifficulties = false OR q.difficulty = ANY(CAST(:difficulties AS text[])))
        """, nativeQuery = true)
    long countFilteredQuestions(
            @Param("hasCompanies") boolean hasCompanies,
            @Param("companies") String[] companies,
            @Param("hasTags") boolean hasTags,
            @Param("tags") String[] tags,
            @Param("hasDifficulties") boolean hasDifficulties,
            @Param("difficulties") String[] difficulties
    );

    @Query(value = """
        SELECT * FROM leetcode_questions q
        WHERE (:hasCompanies = false OR q.companies && CAST(:companies AS text[]))
          AND (:hasTags = false OR q.tags && CAST(:tags AS text[]))
          AND (:hasDifficulties = false OR q.difficulty = ANY(CAST(:difficulties AS text[])))
          AND (:hasExclude = false OR NOT (q.question_id::text = ANY(CAST(:excludeIds AS text[]))))
        ORDER BY RANDOM()
        LIMIT :limit
        """, nativeQuery = true)
    List<LeetCodeQuestion> findRandomFilteredQuestions(
            @Param("hasCompanies") boolean hasCompanies,
            @Param("companies") String[] companies,
            @Param("hasTags") boolean hasTags,
            @Param("tags") String[] tags,
            @Param("hasDifficulties") boolean hasDifficulties,
            @Param("difficulties") String[] difficulties,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") String[] excludeIds,
            @Param("limit") int limit
    );
}