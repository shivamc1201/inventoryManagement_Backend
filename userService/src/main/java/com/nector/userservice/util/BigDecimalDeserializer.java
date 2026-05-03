package com.nector.userservice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom deserializer for BigDecimal that handles boolean values.
 * Converts boolean false to BigDecimal.ZERO and boolean true to null.
 * Also handles string and numeric values.
 */
public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_FALSE) {
            return BigDecimal.ZERO;
        }
        if (p.currentToken() == JsonToken.VALUE_TRUE) {
            return null;
        }
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDecimalValue();
        }
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
