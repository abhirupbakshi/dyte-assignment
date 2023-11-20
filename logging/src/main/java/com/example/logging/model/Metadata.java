package com.example.logging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor(force = true)
@Getter
@Setter
@Accessors(chain = true)
public class Metadata {

    @JsonProperty("parentResourceId")
    @NotBlank(message = "Parent resource id cannot be empty")
    private String parentResourceId;
}
