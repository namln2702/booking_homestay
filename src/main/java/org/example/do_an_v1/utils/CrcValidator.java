package org.example.do_an_v1.utils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CrcValidator {

    private static final int POLYNOMIAL = 0x1021;
    private static final int INITIAL_CRC = 0xFFFF;

    public static boolean isValid(String fullPayload, String providedCrc) {
        if (fullPayload == null || providedCrc == null) {
            return false;
        }

        String normalized = providedCrc.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 4) {
            return false;
        }

        int crcTagIndex = fullPayload.indexOf("63");
        if (crcTagIndex < 0 || crcTagIndex + 4 > fullPayload.length()) {
            return false;
        }

        int lengthStart = crcTagIndex + 2;
        int lengthEnd = lengthStart + 2;
        int valueLength;
        try {
            valueLength = Integer.parseInt(fullPayload.substring(lengthStart, lengthEnd));
        } catch (NumberFormatException ex) {
            return false;
        }

        if (valueLength != 4 || lengthEnd + valueLength > fullPayload.length()) {
            return false;
        }

        String payloadForCrc = fullPayload.substring(0, lengthEnd);
        String computed = computeCrc(payloadForCrc);
        return computed.equalsIgnoreCase(normalized);
    }

    public static String computeCrc(String payload) {
        if (payload == null) {
            return "";
        }

        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        int crc = INITIAL_CRC;

        for (byte aByte : bytes) {
            crc ^= (aByte & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }

        return String.format("%04X", crc);
    }
}
