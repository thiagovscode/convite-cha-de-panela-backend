package br.com.convite.entrypoint.api.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RsvpResponse {
    private String id;
    private String name;
    private Boolean confirmed;
    private String message;
    private LocalDateTime createdAt;
}
