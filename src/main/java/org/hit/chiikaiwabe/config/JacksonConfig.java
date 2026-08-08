package org.hit.chiikaiwabe.config;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.hit.chiikaiwabe.constant.CommonConstant;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    public static class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public Class<?> handledType() {
            return LocalDateTime.class;
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateStr = p.getText();
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }
            dateStr = dateStr.trim();
            try {
                if (dateStr.endsWith("Z")) {
                    dateStr = dateStr.substring(0, dateStr.length() - 1);
                }
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                try {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception ex) {
                    throw new IOException("Cannot parse date: " + dateStr, ex);
                }
            }
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstant.PATTERN_DATE_TIME);
            builder.serializers(new LocalDateTimeSerializer(formatter));
            builder.deserializers(new LenientLocalDateTimeDeserializer());
        };
    }
}
