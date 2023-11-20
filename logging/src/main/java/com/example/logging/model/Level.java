package com.example.logging.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum Level {

    TRACE("trace"), DEBUG("debug"), INFO("info"), WARN("warn"), ERROR("error"), FATAL("fatal");

    public static final List<Level> hierarchy = List.of(TRACE, DEBUG, INFO, WARN, ERROR, FATAL);
    private String value;

    Level(String value) {

        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Level fromValue(String value) {

        for (Level level : Level.values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }

        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
