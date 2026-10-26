package com.arnesh.interviewsphere.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.arnesh.interviewsphere.config.AppProperties;
import com.arnesh.interviewsphere.dto.response.AudioTranscriptionResponse;
import com.arnesh.interviewsphere.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAiChatModel chatModel;
    private final AppProperties appProperties;
    private final WebClient.Builder webClientBuilder;

    
    public String callChatGpt(String systemPrompt, String userContent, double temperature, String jsonSchema) {
        try {
            System.out.println(userContent);
            OpenAiChatModel.ResponseFormat responseFormat = OpenAiChatModel.ResponseFormat.builder()
                    .type(OpenAiChatModel.ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(jsonSchema)
                    .build();

            /*OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(temperature)
                    .responseFormat(responseFormat)
                    .build();*/

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-5-nano")
                    .temperature(1.0)
                    .responseFormat(responseFormat)
                    .build();

            Prompt prompt = new Prompt(
                List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userContent)
                ),
                options
            );

            return chatModel.call(prompt).getResult().getOutput().getText();

        } catch (Exception e) {
            throw new CustomException("AI call failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public AudioTranscriptionResponse callAudioModel1(String wavPath) {
        File file = new File(wavPath);
        if (!file.exists()) {
            throw new CustomException("Audio file not found at path: " + wavPath, HttpStatus.NOT_FOUND);
        }

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

        double durationInSeconds = getWavDurationInSeconds(file);
        double roundedDuration = Math.round(durationInSeconds * 100.0) / 100.0;

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