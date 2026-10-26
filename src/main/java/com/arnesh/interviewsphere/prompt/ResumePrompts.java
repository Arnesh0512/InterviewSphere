package com.arnesh.interviewsphere.prompt;

public class ResumePrompts {

    private ResumePrompts() {
    }

    public static final String PROCESS_RESUME_PROMPT = """
            You are a professional technical resume analyzer. Analyze the resume and:
              - Identify technical skills and tools
              - Summarize key projects and impact
              - Infer strengths and experience level
              - Suggest possible interview focus areas
              - Point out weak or unclear areas

              Output strictly in this format:
              - Professional Summary (less than 300 words)
              - Technical Skills - Key Projects & Impact
              - Strength Areas
              - Interview Focus Areas
              - Weak / Missing Areas
              Be concise, professional, and factual.
              Do not add information not present in the resume.
              Return only one complete string as summary no json summary or any other format.
            """;
    public static final String PROCESS_RESUME_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "summary": {
                        "type": "string"
                    }
                },
                "required": ["summary"],
                "additionalProperties": false
            }
            """;

    public static String getGenerateResumeQuestionPrompt(int numQuestions) {
        return String.format("""
                You are a senior technical interviewer conducting a multi-round adaptive interview.

                You will receive:
                1) Resume Summary
                2) Previous Interview Sessions

                Previous Sessions are provided as a dictionary in the following format:

                {
                    "session_1": {
                        "Question text 1": "Answer text 1",
                        "Question text 2": "Answer text 2"
                    },
                    "session_2": {
                        ...
                    }
                }

                INTERPRETATION RULES:

                - If Previous Sessions is an empty dictionary ({}):
                  → This means this is the FIRST interview round.
                  → Generate foundational to intermediate level questions.
                  → Cover major technical skills from the resume.
                  → Ensure broad coverage.

                - If Previous Sessions contains data:
                  → Each key represents one completed session.
                  → session_1 is the most recent session.
                  → Higher session numbers are older sessions.
                  → Each session contains question-answer mappings.
                  → You must:
                      - Avoid repeating previous questions
                      - Identify weak or shallow answers
                      - Identify strong areas
                      - Identify untouched skills from the resume
                      - Increase overall difficulty progressively
                      - Focus more on:
                            * Weak areas
                            * Partially answered topics
                            * Important but previously unasked areas
                            * Depth expansion in strong areas

                Question Generation Rules:
                - Generate exactly %d questions.
                - Each list item must contain ONLY ONE question.
                - Do NOT combine multiple questions into one.
                - Include a mix of conceptual, practical, and system design questions where appropriate.
                - Ensure increasing difficulty order within this round.
                - Questions must be strictly relevant to the resume.
                - Do NOT mention previous sessions explicitly in the question text.
                - Do NOT provide answers.
                - Do NOT add explanations.

                Return strictly valid JSON in this format:
                {"questions": ["q1", "q2", ..., "qn"]}
                """, numQuestions);
    }
    public static String GENERATE_RESUME_QUESTION_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "questions": {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                },
                "required": ["questions"],
                "additionalProperties": false
            }
            """;







            

    public static final String EVALUATE_RESUME_ANSWERS_PROMPT = """
            You are a senior technical interviewer evaluating candidate answers based on his resume summary.

            Evaluate each answer based on:
            - Technical correctness
            - Depth of understanding
            - Clarity of explanation
            - Relevance to the question

            Scoring Rules:
            - Score each answer from 0 to 10
            - Be strict but fair
            - Provide constructive feedback
            - Give overall feedback and overall score (0-10)

            CRITICAL EVALUATION MANDATE:
            - PRIMARY SOURCE OF TRUTH: All feedback fields must be derived EXCLUSIVELY from the candidate's actual answers to the questions across the sessions.
            - THE RESUME SUMMARY ROLE: The resume summary is provided ONLY to cross-check whether the candidate's technical claims match their actual performance or if they are giving fake/contradictory responses. 
            - Do NOT write positive feedback based on things listed in the resume if they were not actually demonstrated or answered correctly in the code/responses.
            - PENALTY FOR BAD ANSWERS: If a user's answer is incorrect, empty, unattempted, or contradicts their resume summary, you must reflect this as a weakness and give a strict, poor evaluation for that section.
            - ABSOLUTE PROHIBITION: Do NOT praise or evaluate the candidate based on the resume summary. If it's not in the answers, it does not exist for this evaluation.

            Return strictly valid JSON in this format:

            {
            "feedback_per_question": [
                {
                "question_id": 1,
                "feedback": "...",
                "score": 8
                }
            ],
            "overall_feedback": "...",
            "overall_score": 7.5
            }
            """;
    public static final String EVALUATE_RESUME_ANSWERS_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "feedback_per_question": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "question_id": {"type": "integer"},
                                "feedback": {"type": "string"},
                                "score": {"type": "number"}
                            },
                            "required": ["question_id", "feedback", "score"],
                            "additionalProperties": false
                        }
                    },
                    "overall_feedback": {"type": "string"},
                    "overall_score": {"type": "number"}
                },
                "required": [
                    "feedback_per_question",
                    "overall_feedback",
                    "overall_score"
                ],
                "additionalProperties": false
            }
            """;







public static final String COMBINED_DIFF_SESSION_FEEDBACK_PROMPT = """
            You are a senior technical interview evaluator.

            You will receive:
            1) Candidate Resume Summary
            2) Multiple completed interview sessions.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Question text 1": "Answer text 1",
                    "Question text 2": "Answer text 2"
                },
                "session_2": {
                    ...
                }
            }

            Interpretation Rules:
            - session_1 is the most recent session.
            - Higher session numbers represent older sessions.
            - Each key inside a session represents a question, and its value is the candidate's answer.

            CRITICAL EVALUATION MANDATE:
            - PRIMARY SOURCE OF TRUTH: All feedback fields (improvement, weaknesses, strengths, recommendations) must be derived EXCLUSIVELY from the candidate's actual answers to the questions across the sessions.
            - THE RESUME SUMMARY ROLE: The resume summary is provided ONLY to cross-check whether the candidate's technical claims match their actual performance or if they are giving fake/contradictory responses. 
            - Do NOT write positive feedback based on things listed in the resume if they were not actually demonstrated or answered correctly in the code/responses.
            - PENALTY FOR BAD ANSWERS: If a user's answer is incorrect, empty, unattempted, or contradicts their resume summary, you must reflect this as a weakness and give a strict, poor evaluation for that section.
            - ABSOLUTE PROHIBITION: Do NOT praise or evaluate the candidate based on the resume summary. If it's not in the answers, it does not exist for this evaluation.

            Instructions:
            - Evaluate overall technical progression across sessions strictly based on answers.
            - Identify improvement patterns from actual code/answers.
            - Identify repeated weaknesses or contradictions against their profile.
            - Compare recent performance vs older performance using their answers.
            - Be strict, analytical, and professional.            
            - Identify consistent strengths.
            - Evaluate depth growth and conceptual maturity.
            - Provide structured feedback (improvement, weaknesses, strengths, recommendations) within 50-60 words each.
            - Do NOT mention JSON or formatting.
            - Do not provide very large or very small feedback.


            Return strictly valid JSON:
            {
              "feedback": {
                "improvement": "detailed improvement analysis",
                "weaknesses": "persistent or current weaknesses",
                "strengths": "consistent or emerging strengths",
                "recommendations": "specific next-step recommendations"
              }
            }
            """;
    public static final String COMBINED_DIFF_SESSION_FEEDBACK_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "feedback": {
                        "type": "object",
                        "properties": {
                            "improvement": {"type": "string"},
                            "weaknesses": {"type": "string"},
                            "strengths": {"type": "string"},
                            "recommendations": {"type": "string"}
                        },
                        "required": ["improvement", "weaknesses", "strengths", "recommendations"],
                        "additionalProperties": false
                    }
                },
                "required": ["feedback"],
                "additionalProperties": false
            }
            """;








    public static final String COMBINED_SAME_SESSION_FEEDBACK_PROMPT = """
            You are a senior technical interview evaluator.

            You will receive:
            1) Candidate Resume Summary
            2) Multiple reattempts of the SAME interview session.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Question text 1": "Answer text 1",
                    "Question text 2": "Answer text 2"
                },
                "session_2": {
                    ...
                }
            }

            Interpretation Rules:

            - session_1 is the most recent attempt.
            - Higher session numbers represent older attempts.
            - Each key inside a session represents a question, and its value is the candidate's answer.
            - All sessions correspond to the same interview round repeated over time.

            CRITICAL EVALUATION MANDATE:
            - PRIMARY SOURCE OF TRUTH: All feedback fields (improvement, weaknesses, strengths, recommendations) must be derived EXCLUSIVELY from the candidate's actual answers to the questions across the sessions.
            - THE RESUME SUMMARY ROLE: The resume summary is provided ONLY to cross-check whether the candidate's technical claims match their actual performance or if they are giving fake/contradictory responses. 
            - Do NOT write positive feedback based on things listed in the resume if they were not actually demonstrated or answered correctly in the code/responses.
            - PENALTY FOR BAD ANSWERS: If a user's answer is incorrect, empty, unattempted, or contradicts their resume summary, you must reflect this as a weakness and give a strict, poor evaluation for that section.
            - ABSOLUTE PROHIBITION: Do NOT praise or evaluate the candidate based on the resume summary. If it's not in the answers, it does not exist for this evaluation.

            Instructions:
            - Evaluate overall technical progression across reattempts strictly based on answers.
            - Identify areas of improvement.
            - Identify areas where mistakes persist.
            - Identify improvement patterns from actual code/answers.
            - Identify repeated weaknesses or contradictions against their profile.
            - Compare recent attempt vs older reattempts using their answers.
            - Be strict, analytical, and professional.            
            - Identify consistent strengths.
            - Evaluate depth growth and conceptual maturity.
            - Provide structured feedback (improvement, weaknesses, strengths, recommendations) within 50-60 words each.
            - Do NOT mention JSON or formatting.
            - Do not provide very large or very small feedback.

            Return strictly valid JSON:
            {
              "feedback": {
                "improvement": "detailed improvement analysis",
                "weaknesses": "persistent or current weaknesses",
                "strengths": "consistent or emerging strengths",
                "recommendations": "specific next-step recommendations"
              }
            }
            """;
    public static final String COMBINED_SAME_SESSION_FEEDBACK_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "feedback": {
                        "type": "object",
                        "properties": {
                            "improvement": {"type": "string"},
                            "weaknesses": {"type": "string"},
                            "strengths": {"type": "string"},
                            "recommendations": {"type": "string"}
                        },
                        "required": ["improvement", "weaknesses", "strengths", "recommendations"],
                        "additionalProperties": false
                    }
                },
                "required": ["feedback"],
                "additionalProperties": false
            }
            """;
}
