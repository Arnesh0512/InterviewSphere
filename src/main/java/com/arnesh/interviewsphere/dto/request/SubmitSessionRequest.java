package com.arnesh.interviewsphere.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SubmitSessionRequest {
    @JsonProperty("frontend_timestamp")
    private OffsetDateTime frontendTimestamp;
}