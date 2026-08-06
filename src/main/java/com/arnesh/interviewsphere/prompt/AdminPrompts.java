package com.arnesh.interviewsphere.prompt;

public class AdminPrompts {

    private AdminPrompts() {
    }

    public static final String VALIDATE_ROLE_SKILLS_PROMPT = """
		You are an expert technical recruiter.

		You will be given a <role> required for a position
		and a <list of skills>.

		Determine if these skills are appropriate for the role.

		Return strictly JSON:

		{"valid": true/false}
		""";
    public static final String VALIDATE_ROLE_SKILLS_RESPONSE = """
        {
            "type": "object",
            "properties": {
                "valid": {
                        "type": "boolean"
                }
            },
            "required": ["valid"],
            "additionalProperties": false
        }
        """;

    public static final String GENERATE_RESUME_QUESTIONS_PROMPT = """
            You are an expert technical recruiter designing automated resume screening for a company.

            Generate resume screening questions that can be answered directly by analyzing
            the candidate's resume text.

            The goal is to later evaluate if the resume demonstrates the required skills
            and experience for the role.

            Each question should help verify:

            - whether the candidate has experience with the listed skills
            - whether they have worked on relevant projects
            - whether their experience aligns with the role

            IMPORTANT RULES:
            - Questions must be answerable using only the resume text
            - Avoid subjective questions like "tell me about..."
            - Prefer verification questions like:
              - "Does the candidate mention experience with X?"
              - "What project demonstrates use of X?"
              - "How many years of experience does the candidate show in X?"

            Return strictly JSON:

            {
            "questions": ["q1", "q2", "q3"]
            }
            """;
    public static final String GENERATE_RESUME_QUESTIONS_RESPONSE = """
		{
			"type": "object",
			"properties": {
				"questions": {
					"type": "array",
					"items": {"type": "string"}
				}
			},
			"required": ["questions"],
            "additionalProperties": false
		}
		""";

    public static final String GENERATE_CONCEPT_QUESTIONS_PROMPT = """
            You are a senior technical interviewer.

            Generate concept interview questions that test
            deep understanding of core technical concepts.

            Return strictly JSON:

            {
            "questions": ["q1","q2","q3"]
            }
            """;
    public static final String GENERATE_CONCEPT_QUESTIONS_RESPONSE = """
		{
			"type": "object",
			"properties": {
				"questions": {
					"type": "array",
					"items": {"type": "string"}
				}
			},
			"required": ["questions"],
            "additionalProperties": false
		}
		""";

    public static final String GENERATE_HR_QUESTIONS_PROMPT = """
            You are an HR interviewer.

            Generate HR interview questions that evaluate:
            communication,
            behavior,
            teamwork,
            and work ethics.

            Return strictly JSON:

            {
            "questions": ["q1","q2","q3"]
            }
            """;
    public static final String GENERATE_HR_QUESTIONS_RESPONSE = """
		{
			"type": "object",
			"properties": {
				"questions": {
					"type": "array",
					"items": {"type": "string"}
				}
			},
			"required": ["questions"],
            "additionalProperties": false
		}
		""";
}
