package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.config.AppProperties;
import com.arnesh.interviewsphere.dto.response.AudioTranscriptionResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAiChatModel chatModel;
    private final AppProperties appProperties;
    private final WebClient.Builder webClientBuilder;

    /**
     * Executes structured Chat completions using GPT models[cite: 9].
     */
    public String callChatGpt(String systemPrompt, String userContent, double temperature) {
        try {
            // In Spring AI 1.0.0-M1, ResponseFormat takes a String parameter
            // ("json_object")
            OpenAiApi.ChatCompletionRequest.ResponseFormat responseFormat = new OpenAiApi.ChatCompletionRequest.ResponseFormat(
                    "json_object");

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withModel("gpt-4o-mini")
                    .withTemperature((float) temperature)
                    .withResponseFormat(responseFormat)
                    .build();

            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(systemPrompt), new UserMessage(userContent)),
                    options);

            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getContent();

        } catch (Exception e) {
            throw new CustomException("Failed to call ChatGPT: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Transcribes WAV audio files using OpenAI Audio API and computes audio
     * duration[cite: 9].
     */
    public AudioTranscriptionResponse callAudioModel1(String wavPath) {
        File file = new File(wavPath);
        if (!file.exists()) {
            throw new CustomException("Audio file not found at path: " + wavPath, HttpStatus.NOT_FOUND);
        }

        // 1. Call OpenAI Whisper API for transcription[cite: 9]
        WebClient client = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getOpenAiApiKey())
                .build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("model", "whisper-1");

        String transcript;
        try {
            Map<String, Object> apiResponse = client.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            transcript = apiResponse != null && apiResponse.containsKey("text")
                    ? (String) apiResponse.get("text")
                    : "";

        } catch (Exception e) {
            throw new CustomException("Audio transcription failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 2. Compute audio duration using Java Sound API[cite: 9]
        double durationInSeconds = getWavDurationInSeconds(file);
        double roundedDuration = Math.round(durationInSeconds * 100.0) / 100.0;

        // 3. Format output response[cite: 9]
        AudioTranscriptionResponse.SegmentData segment = AudioTranscriptionResponse.SegmentData.builder()
                .start(0.0)
                .end(roundedDuration)
                .text(transcript)
                .build();

        return AudioTranscriptionResponse.builder()
                .transcript(transcript)
                .segmentedData(List.of(segment))
                .build();
    }

    /**
     * Helper to compute WAV audio duration in seconds.
     */
    private double getWavDurationInSeconds(File file) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            return (double) frames / format.getFrameRate();
        } catch (Exception e) {
            return 0.0;
        }
    }
}