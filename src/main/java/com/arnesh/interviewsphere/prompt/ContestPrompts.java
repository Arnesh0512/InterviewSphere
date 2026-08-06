package com.arnesh.interviewsphere.prompt;

public class ContestPrompts {

    private ContestPrompts() {
    }

    public static final String GENERATE_SUMMARY_PROMPT = """
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
    public static final String GENERATE_SUMMARY_RESPONSE = """
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

    public static final String EVALUATE_RESUME_SCORE_PROMPT = """
            You are a senior technical interviewer.

            You will receive:

            1) Candidate Resume
            2) Interview Questions
            3) Company Name, Role, and Required Skills.

            For EACH question:
            Evaluate whether the resume shows evidence that the candidate could answer it.

            Scoring rules:
            - Score each question from 0 to 10
            - 0 = no evidence in resume
            - 10 = strong clear evidence
            - Base scores only on resume content
            - Do NOT assume missing information
            - Provide actionable feedback

            Also provide:
            - Overall feedback

            IMPORTANT:
            - Do not hallucinate missing answers
            - Evaluate only based on given response
            - Return strictly valid JSON

            {
              "results":
                [
                    {
                    "question_id":integer,
                    "feedback": "string",
                    "score":number
                    }, ....
                ],
                "overall_feedback": "string"
            }
            """;
    public static final String EVALUATE_RESUME_SCORE_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "results": {
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
                            ],
                            "additionalProperties": false
                        }
                    },
                    "overall_feedback": {"type": "string"}
                },
                "required": [
                    "results",
                    "overall_feedback"
                ],
                "additionalProperties": false
            }
            """;

    public static final String EVALUATE_CODING_SCORE_PROMPT = """
            You are a senior coding interviewer.

            You will receive list of such dictionaries
            {"question_id": ,"problem_description":, "answer": ,"language": }

            Evaluate each solution.

            Scoring rules:
            Score each question from 0 to 10
            0 = completely wrong or no answer
            10 = correct and optimal
            Provide actionable feedback also

            Also provide:
            - Overall feedback

            IMPORTANT:
            - Do not hallucinate missing answers
            - Evaluate only based on given response
            - Return strictly valid JSON

            {
              "results":
                [
                    {
                    "question_id":integer,
                    "feedback": "string",
                    "score":number
                    }, ....
                ],
                "overall_feedback": "string"
            }
            """;
    public static final String EVALUATE_CODING_SCORE_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "results": {
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
                            ],
                            "additionalProperties": false
                        }
                    },
                    "overall_feedback": {"type": "string"}
                },
                "required": [
                    "results",
                    "overall_feedback"
                ],
                "additionalProperties": false
            }
            """;

    public static final String EVALUATE_CONCEPT_SCORE_PROMPT = """
            You are a senior technical interviewer.

            You will receive list of such dictionaries
            {"question_id": ,"question":, "answer": }

            Evaluate each solution.

            Scoring rules:
            Score each question from 0 to 10
            0 = completely wrong or no answer
            10 = conceptually correct and optimal
            Score each question independently.
            Provide actionable feedback also

            Also provide:
            - Overall feedback

            IMPORTANT:
            - Do not hallucinate missing answers
            - Evaluate only based on given response
            - Return strictly valid JSON

            {
              "results":
                [
                    {
                    "question_id":integer,
                    "feedback": "string",
                    "score":number
                    }, ....
                ],
                "overall_feedback": "string"
            }
            """;
    public static final String EVALUATE_CONCEPT_SCORE_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "results": {
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
                            ],
                            "additionalProperties": false
                        }
                    },
                    "overall_feedback": {"type": "string"}
                },
                "required": [
                    "results",
                    "overall_feedback"
                ],
                "additionalProperties": false
            }
            """;

    public static final String EVALUATE_HR_SCORE_PROMPT = """
            You are a senior HR interviewer evaluating candidate responses.

            You will receive a list of dictionaries in the following format:

            {
             "question_id": integer,
             "question": "string",
             "transcript": "string",
             "segmented_data":[
               {"start": number, "end": number, "text": "string"},
               ...
             ]
            }
            transcript is made using segmented_data text field

            It is given candidate was given exactly 1 minute to speak his answer. ##########

            For question_id 1 the question will be:
            "Introduce Yourself"

            For this question you will also receive a candidate resume summary.

            Evaluation criteria:

            1. Communication clarity
            2. Confidence (use speaking duration and pacing)
            3. Relevance to the question
            4. Professional tone
            5. Completeness of the response

            Confidence analysis rule:
            - Longer pauses or very short answers may indicate low confidence
            - Smooth continuous speech with moderate pacing indicates higher confidence

            For question_id 1:
            - Evaluate whether the candidate introduction aligns with the resume summary.

            Scoring rules:
            Score each question from 0 to 10
            0 = no response or irrelevant answer
            10 = confident, structured, and highly relevant response
            Score each question independently.

            Provide actionable feedback also

            Also provide:
            - Overall feedback

            IMPORTANT:
            - Do not hallucinate missing answers
            - Evaluate only based on given response
            - Return strictly valid JSON

            {
              "results":
                [
                    {
                    "question_id":integer,
                    "feedback": "string",
                    "score":number
                    }, ....
                ],
                "overall_feedback": "string"
            }
            """;
    public static final String EVALUATE_HR_SCORE_RESPONSE = """
            {
                "type": "object",
                "properties": {
                    "results": {
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
                            ],
                            "additionalProperties": false
                        }
                    },
                    "overall_feedback": {"type": "string"}
                },
                "required": [
                    "results",
                    "overall_feedback"
                ],
                "additionalProperties": false
            }
            """;
}
