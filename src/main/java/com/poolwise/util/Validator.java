package com.poolwise.util;

import com.poolwise.exception.InvalidUserException;

/**
 * Static validation utilities used across the application.
 * Demonstrates: static methods, exception handling, input sanitisation.
 */
public final class Validator {

    private Validator() {} // Utility class — no instances

    /** Throws if the value is null or blank. */
    public static String requireNonBlank(String value, String fieldName) throws InvalidUserException {
        if (value == null || value.trim().isEmpty())
            throw new InvalidUserException(fieldName, fieldName + " must not be empty.");
        return value.trim();
    }

    /** Throws if the value is not a positive double. */
    public static double requirePositiveDouble(String raw, String fieldName) throws InvalidUserException {
        try {
            double v = Double.parseDouble(raw.trim());
            if (v <= 0) throw new InvalidUserException(fieldName, fieldName + " must be positive.");
            return v;
        } catch (NumberFormatException e) {
            throw new InvalidUserException(fieldName, fieldName + " must be a number.");
        }
    }

    /** Throws if the value is not a non-negative double (zero is allowed). */
    public static double requireNonNegDouble(String raw, String fieldName) throws InvalidUserException {
        try {
            double v = Double.parseDouble(raw.trim());
            if (v < 0) throw new InvalidUserException(fieldName, fieldName + " must be >= 0.");
            return v;
        } catch (NumberFormatException e) {
            throw new InvalidUserException(fieldName, fieldName + " must be a number.");
        }
    }

    /** Throws if the value is not a positive integer. */
    public static int requirePositiveInt(String raw, String fieldName) throws InvalidUserException {
        try {
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) throw new InvalidUserException(fieldName, fieldName + " must be a positive integer.");
            return v;
        } catch (NumberFormatException e) {
            throw new InvalidUserException(fieldName, fieldName + " must be an integer.");
        }
    }

    /** Throws if the value is not in [lo, hi]. */
    public static double requireInRange(String raw, String fieldName, double lo, double hi) throws InvalidUserException {
        try {
            double v = Double.parseDouble(raw.trim());
            if (v < lo || v > hi)
                throw new InvalidUserException(fieldName,
                        fieldName + " must be between " + lo + " and " + hi + ".");
            return v;
        } catch (NumberFormatException e) {
            throw new InvalidUserException(fieldName, fieldName + " must be a number.");
        }
    }

    /** Throws if phone is not exactly 10 digits. */
    public static String requirePhone(String raw, String fieldName) throws InvalidUserException {
        String s = raw.trim().replaceAll("[\\s\\-]", "");
        if (!s.matches("\\d{10}"))
            throw new InvalidUserException(fieldName, fieldName + " must be exactly 10 digits.");
        return s;
    }

    /** Throws if the string is not a valid HH:MM time. */
    public static String requireTime(String raw, String fieldName) throws InvalidUserException {
        if (raw == null || !raw.trim().matches("([01]\\d|2[0-3]):[0-5]\\d"))
            throw new InvalidUserException(fieldName, fieldName + " must be in HH:MM format (e.g. 08:30).");
        return raw.trim();
    }

    /** Strip dangerous CSV characters from a string. */
    public static String sanitize(String s) {
        if (s == null) return "";
        return s.trim().replace(",", ";").replace("\n", " ").replace("\r", "");
    }
}
