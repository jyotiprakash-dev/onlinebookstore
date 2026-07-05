package com.bittercode.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class UserInputValidator {

    private static final int MAX_DB_FIELD_LENGTH = 100;
    private static final int MAX_ADDRESS_LENGTH = 250;

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z .'-]{0,99}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private UserInputValidator() {
    }

    public static ValidationResult validateCustomerLogin(String username, String password) {
        List<String> errors = new ArrayList<String>();

        if (isBlank(username)) {
            errors.add("Username is required.");
        } else if (trimToEmpty(username).length() > MAX_DB_FIELD_LENGTH) {
            errors.add("Username must be 100 characters or less.");
        }

        if (isBlank(password)) {
            errors.add("Password is required.");
        } else if (password.length() > MAX_DB_FIELD_LENGTH) {
            errors.add("Password must be 100 characters or less.");
        }

        return new ValidationResult(errors);
    }

    public static ValidationResult validateCustomerRegistration(String email, String password, String firstName,
            String lastName, String address, String phone, String acceptance) {
        List<String> errors = new ArrayList<String>();
        String cleanEmail = trimToEmpty(email);
        String cleanFirstName = trimToEmpty(firstName);
        String cleanLastName = trimToEmpty(lastName);
        String cleanAddress = trimToEmpty(address);
        String cleanPhone = trimToEmpty(phone);

        if (cleanEmail.isEmpty()) {
            errors.add("Email is required.");
        } else if (cleanEmail.length() > MAX_DB_FIELD_LENGTH) {
            errors.add("Email must be 100 characters or less.");
        } else if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            errors.add("Enter a valid email address.");
        }

        if (isBlank(password)) {
            errors.add("Password is required.");
        } else if (password.length() < 8) {
            errors.add("Password must be at least 8 characters.");
        } else if (password.length() > MAX_DB_FIELD_LENGTH) {
            errors.add("Password must be 100 characters or less.");
        }

        validateName("First name", cleanFirstName, errors);
        validateName("Last name", cleanLastName, errors);

        if (cleanAddress.isEmpty()) {
            errors.add("Address is required.");
        } else if (cleanAddress.length() > MAX_ADDRESS_LENGTH) {
            errors.add("Address must be 250 characters or less.");
        }

        if (cleanPhone.isEmpty()) {
            errors.add("Mobile number is required.");
        } else if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            errors.add("Enter a valid 10-digit mobile number.");
        }

        if (isBlank(acceptance)) {
            errors.add("Please accept the Terms & Conditions.");
        }

        return new ValidationResult(errors);
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static void validateName(String fieldName, String value, List<String> errors) {
        if (value.isEmpty()) {
            errors.add(fieldName + " is required.");
        } else if (value.length() > MAX_DB_FIELD_LENGTH) {
            errors.add(fieldName + " must be 100 characters or less.");
        } else if (!NAME_PATTERN.matcher(value).matches()) {
            errors.add(fieldName + " can contain only letters, spaces, apostrophes, periods, and hyphens.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class ValidationResult {
        private final List<String> errors;

        private ValidationResult(List<String> errors) {
            this.errors = Collections.unmodifiableList(errors);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getMessage() {
            return String.join("<br/>", errors);
        }
    }
}
