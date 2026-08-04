package com.arnesh.interviewsphere.prompt;

public class ConceptPrompts {

    private ConceptPrompts() {
    }

    public static String getGenerateConceptTopicQuestionsPrompt(int numQuestions) {
        return String.format("""
                You are a senior computer science technical interviewer.

                Based on the given CS topics, generate %d\s
                deep technical interview questions.

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
                - If Previous Sessions is an empty dictionary ({}):
                → This means this is the FIRST interview round.

                - If Previous Sessions contains data:
                → session_1 is the most recent session.
                → Higher session numbers represent older sessions.
                → Each key inside a session is a question and its value is the candidate’s answer.
                → You must:
                    - Avoid repeating previous questions
                    - Identify weak or shallow answers
                    - Identify strong architectural understanding
                    - Identify untouched components of the topic.
                    - Increase overall difficulty progressively
                    - Focus more on:
                            * Weak explanations
                            * Theory gaps
                            * Edge cases not discussed
                            * Indepth topic-level reasoning

                Questions should test:
                - Core conceptual understanding
                - Internal working
                - Design decisions
                - Trade-offs
                - Complexity analysis
                - Edge cases
                - Real-world application

                Question Generation Rules:
                - Generate exactly %d questions.
                - Each list item must contain ONLY ONE question.
                - Do NOT combine multiple questions into one.
                - Ensure increasing difficulty order within this round.
                - Questions must be highly specific to the repository.
                - Do NOT mention previous sessions explicitly in the question text.
                - Do NOT provide answers.
                - Do NOT add explanations.

                Output format:

                {
                    "questions": ["question1", "question2", ...]
                }
                """, numQuestions, numQuestions);
    }

    public static final String GENERATE_CONCEPT_TOPIC_QUESTIONS_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "questions": {
                        "type": "array",
                        "items": {"type": "string"}
                    }
                },
                "required": ["questions"]
            }
            """;

    public static final String EVALUATE_CONCEPT_TOPIC_ANSWERS_PROMPT = """
            You are a senior computer science technical interviewer.

            You are evaluating answers related to specific Computer Science topics.

            Evaluate each answer strictly based on:

            - Understanding of topic fundamentals
            - Correctness of technical explanation
            - Depth of implementation knowledge
            - Design reasoning
            - Trade-offs awareness
            - Scalability, security, and edge cases

            Scoring Rules:
            - Score each answer from 0 to 10
            - Be strict and realistic
            - Penalize vague or generic answers
            - Reward deep architectural and conceptual understanding
            - Provide actionable feedback

            Also provide:
            - Overall feedback
            - Overall score (0–10)

            IMPORTANT:
            - Do not hallucinate missing answers
            - Evaluate only based on given response
            - Return strictly valid JSON

            Format:

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
    public static final String EVALUATE_CONCEPT_TOPIC_ANSWERS_RESPONSE = """
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
                            "required": [
                                "question_id",
                                "feedback",
                                "score"
                            ]
                        }
                    },
                    "overall_feedback": {"type": "string"},
                    "overall_score": {"type": "number"}
                },
                "required": [
                    "feedback_per_question",
                    "overall_feedback",
                    "overall_score"
                ]
            }
            """;

    public static final String COMBINED_DIFF_SESSION_FEEDBACK_PROMPT = """
            You are a senior technical interview evaluator.

            You will receive:
            1) Selected conceptual topics
            2) Multiple completed conceptual interview sessions.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Question text": "Answer text"
                },
                "session_2": {
                    ...
                }
            }

            Interpretation Rules:

            - session_1 is the most recent session.
            - Higher session numbers represent older sessions.
            - Each key inside a session is a question and its value is the candidate's answer.
            - Sessions may contain different questions across rounds.

            Instructions:

            - Evaluate overall conceptual progression across sessions.
            - Identify depth improvement in theoretical understanding.
            - Identify repeated conceptual weaknesses.
            - Identify consistent strengths.
            - Evaluate clarity of explanation and correctness.
            - Evaluate ability to connect concepts across topics.
            - Compare recent sessions with older sessions.
            - Be strict, analytical, and professional.
            - Provide structured feedback (improvement, weaknesses, strengths, recommendations).
            - Do NOT mention JSON or formatting.

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
                        "additionalProperties": False
                    }
                },
                "required": ["feedback"],
                "additionalProperties": False
            }
            """;

    public static final String COMBINED_SAME_SESSION_FEEDBACK_PROMPT = """
            You are a senior technical interview evaluator.

            You will receive:
            1) Selected conceptual topics
            2) Multiple reattempts of the SAME conceptual interview session.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Question text": "Answer text"
                },
                "session_2": {
                    ...
                }
            }

            Interpretation Rules:

            - session_1 is the most recent attempt.
            - Higher session numbers represent older attempts.
            - All sessions correspond to the SAME question set.
            - Only answers differ between attempts.

            Instructions:

            - Evaluate conceptual improvement across attempts.
            - Identify corrections of previous misunderstandings.
            - Identify persistent conceptual errors.
            - Evaluate improvement in explanation clarity and structure.
            - Evaluate depth growth in theoretical reasoning.
            - Compare the latest attempt clearly with older attempts.
            - Be strict, analytical, and professional.
            - Provide structured feedback (improvement, weaknesses, strengths, recommendations).
            - Do NOT mention JSON or formatting.

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
                        "additionalProperties": False
                    }
                },
                "required": ["feedback"],
                "additionalProperties": False
            }
            """;
}
