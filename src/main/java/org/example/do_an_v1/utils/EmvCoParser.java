package org.example.do_an_v1.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EmvCoParser {

    public static Map<String, String> parse(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        String sanitized = payload.trim();
        Map<String, String> result = new LinkedHashMap<>();
        int index = 0;

        while (index < sanitized.length()) {
            if (index + 4 > sanitized.length()) {
                throw new IllegalArgumentException("Invalid TLV structure at position " + index);
            }

            String tag = sanitized.substring(index, index + 2);
            String lengthStr = sanitized.substring(index + 2, index + 4);
            int length = parseLength(lengthStr);
            index += 4;

            if (index + length > sanitized.length()) {
                throw new IllegalArgumentException("Invalid length for tag " + tag);
            }

            String value = sanitized.substring(index, index + length);
            result.put(tag, value);
            index += length;
        }

        return result;
    }

    private static int parseLength(String length) {
        try {
            int parsed = Integer.parseInt(length);
            if (parsed < 0) {
                throw new IllegalArgumentException("TLV length must be positive");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid TLV length: " + length, ex);
        }
    }
}
