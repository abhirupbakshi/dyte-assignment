package com.example.logging.utility.implementation;

import com.example.logging.exception.MappingUnsuccessfulException;
import com.example.logging.utility.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Map;

@Component
public class JsonUtilsImpl implements JsonUtils {

    private Map.Entry<String, Field> toClassFieldName(String[] json, Class<?> clazz, int i) {

        if (i >= json.length) return null;

        Field classField = null;
        String classFieldName = null;

        for (Field field : clazz.getDeclaredFields()) {

            JsonProperty jsonPropertyAnnotation = field.getAnnotation(JsonProperty.class);
            String jsonField = jsonPropertyAnnotation == null ? null : jsonPropertyAnnotation.value();

            if (jsonField != null && jsonField.equals(json[i])) {

                Map.Entry<String, Field> temp = toClassFieldName(json, field.getType(), i + 1);

                if (temp != null) {
                    classFieldName = field.getName() + "." + temp.getKey();
                    classField = temp.getValue();
                } else {
                    classFieldName = field.getName();
                    classField = field;
                }

                break;
            }
        }

        classFieldName = classFieldName == null || classFieldName.split("\\.").length != (json.length - i) ? null : classFieldName;

        return classFieldName == null ? null : Map.entry(classFieldName, classField);
    }

    @Override
    public Map.Entry<String, Field> toClassField(String json, Class<?> clazz) {


        Assert.notNull(json, "Json field must not be null");
        Assert.notNull(clazz, "Class must not be null");

        Map.Entry<String, Field> result = toClassFieldName(json.split("\\."), clazz, 0);

        if (result == null) {
            throw new MappingUnsuccessfulException("JSON name: " + json + " could not be mapped to the class field name for class: " + clazz.getName());
        }

        return result;
    }
}
