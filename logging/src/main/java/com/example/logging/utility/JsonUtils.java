package com.example.logging.utility;

import java.lang.reflect.Field;
import java.util.Map;

public interface JsonUtils {

    Map.Entry<String, Field> toClassField(String json, Class<?> clazz);
}