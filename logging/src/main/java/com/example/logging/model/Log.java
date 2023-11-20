package com.example.logging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "logs")
public class Log {

    @JsonProperty(value = "uuid", access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    UUID uuid;

    @JsonProperty("level")
    @NotNull(message = "Log level cannot be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    @JsonProperty("message")
    @NotBlank(message = "Log message cannot be empty")
    @Column(name = "message", nullable = false)
    private String message;

    @JsonProperty("resourceId")
    @NotBlank(message = "Resource id cannot be empty")
    @Column(name = "resourceId", nullable = false)
    private String resourceId;

    @JsonProperty("timestamp")
    @NotNull(message = "Resource id cannot be empty")
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @JsonProperty("traceId")
    @NotBlank(message = "Trace id cannot be empty")
    @Column(name = "traceId", nullable = false)
    private String traceId;

    @JsonProperty("spanId")
    @NotBlank(message = "Span id cannot be empty")
    @Column(name = "spanId", nullable = false)
    private String spanId;

    @JsonProperty("commit")
    @NotBlank(message = "Commit cannot be empty")
    @Column(name = "commit", nullable = false)
    private String commit;

    @JsonProperty("metadata")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "parentResourceId", column = @Column(name = "parent_resource_id"))
    })
    private Metadata metadata;
}