package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioTranscriptionResponse {

    private String transcript;
    private List<SegmentData> segmentedData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SegmentData {
        private double start;
        private double end;
        private String text;
    }
}