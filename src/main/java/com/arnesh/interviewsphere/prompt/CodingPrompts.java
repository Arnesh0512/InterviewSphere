package com.arnesh.interviewsphere.prompt;

public class CodingPrompts {

    private CodingPrompts() {
    }

    public static final String EVALUATE_CODING_ANSWERS_PROMPT = """
            You are a senior technical coding interviewer.

            For each coding solution, evaluate:

            - Correctness of logic
            - Time complexity awareness
            - Space complexity awareness
            - Code structure and readability
            - Edge case handling

            Scoring Rules:
            - Score each question from 0 to 10
            - Be strict but fair
            - Penalize incorrect logic heavily
            - Reward optimal solutions
            - Provide clear and concise feedback

            Also provide:
            - Overall feedback
            - Overall score (0-10)

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

            -question_id provided in input should match in output
            """;
    public static final String EVALUATE_CODING_ANSWERS_RESPONSE = """
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
            You are a senior coding interview evaluator.

            You will receive multiple completed coding interview sessions.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Problem:\\nProblem1": "Language: X1\\nAnswer:\\nCode1",
                    "Problem:\\nProblem2": "Language: X2\\nAnswer:\\nCode2",
                    ...
                },
                "session_2": {
                    ...
                }
            }
            The keys(Problem) in each item of each session_i is question given to candidate in that session.
            The keys(Problem) is not at all user response and does not demonstarte user analysis
            The keys(Problem) is a question from question bank
            
            The values(Language and Answer) in each item of each session_i is user response for that Problem(key)
            If value is "Language: \\n Answer:\\n" it means user didnt attempt the Problem at all
            Be strict in evaluation 
            
            CRITICAL RULE FOR UNATTEMPTED SESSIONS:
            - If all answers across all sessions are empty/unattempted (i.e., equal to "Language: \\n Answer:\\n"),
            you must explicitly state in all feedback fields (improvement, weaknesses, strengths, recommendations)
            that the candidate did not provide any code or solutions for evaluation, 
            and therefore no analytical comparison can be made. 
            Do NOT fabricate or hallucinate improvement or strengths if no code exists.

            Interpretation Rules:

            - session_1 is the most recent session.
            - Higher session numbers represent older sessions.
            - Each key inside a session is the full problem description.
            - The value contains the language used and the candidate's full answer.

            Instructions:

            - Evaluate overall coding progression across sessions.
            - Analyze problem-solving approach maturity.
            - Evaluate improvement in:
                * Algorithmic thinking
                * Code structure
                * Edge case handling
                * Optimization
                * Clarity and readability
            - Identify repeated logical mistakes.
            - Identify improvement in time/space complexity reasoning.
            - Compare recent sessions with older sessions.
            - Be strict and analytical.
            - Be strict, analytical, and professional.
            - Provide structured feedback (strengths, weaknesses, progression, recommendations).
            - Do NOT mention JSON or formatting.
            -Do not provide very large or very small feedback.

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
            You are a senior coding interview evaluator.

            You will receive multiple reattempts of the SAME coding interview session.

            Sessions are provided as a dictionary in this format:

            {
                "session_1": {
                    "Problem:\\nProblem1": "Language: X1\\nAnswer:\\nCode1",
                    "Problem:\\nProblem2": "Language: X2\\nAnswer:\\nCode2",
                    ...
                },
                "session_2": {
                    ...
                }
            }
            The keys(Problem) in each item of each session_i is question given to candidate in that session.
            The keys(Problem) is not at all user response and does not demonstarte user analysis
            The keys(Problem) is a question from question bank
            
            The values(Language and Answer) in each item of each session_i is user response for that Problem(key)
            If value is "Language: \\n Answer:\\n" it means user didnt attempt the Problem at all
            Be strict in evaluation 
            
            CRITICAL RULE FOR UNATTEMPTED SESSIONS:
            - If all answers across all sessions are empty/unattempted (i.e., equal to "Language: \\n Answer:\\n"),
            you must explicitly state in all feedback fields (improvement, weaknesses, strengths, recommendations)
            that the candidate did not provide any code or solutions for evaluation, 
            and therefore no analytical comparison can be made. 
            Do NOT fabricate or hallucinate improvement or strengths if no code exists.
            

            Interpretation Rules:

            - session_1 is the most recent attempt.
            - Higher session numbers represent older attempts.
            - All sessions correspond to the SAME set of problems.
            - Only answers and approaches may differ.

            Instructions:

            - Evaluate improvement across attempts.
            - Analyze correction of previous mistakes.
            - Evaluate improvement in:
                * Algorithm choice
                * Code efficiency
                * Edge case handling
                * Code readability
                * Use of appropriate data structures
            - Identify persistent mistakes.
            - Compare latest attempt with older attempts clearly.
            - Evaluate whether complexity improved.
            - Be strict, analytical, and professional.
            - Provide structured feedback (improvement, weaknesses, strengths, recommendations).
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
