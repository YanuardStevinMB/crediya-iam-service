package com.crediya.iam.usecase.user.generaterequest;


import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.shared.Messages;
import com.crediya.iam.usecase.shared.ValidationException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public final class UserValidator {
    private UserValidator() {}

    private static final BigDecimal SALARIO_MIN = new BigDecimal("0.00");
    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000.00");
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /** Valida y normaliza el User en memoria. Lanza ValidationException si falla. */
    public static void validateAndNormalize(User u) {
        if (u == null)
            throw new ValidationException(null, Messages.USER_REQUIRED);

        if (u.getPassword() == null
                || !u.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new ValidationException("password", Messages.PASSWORD_VALIDATED);
        }

        // Documento
        String doc = u.getIdentityDocument();
        if (doc == null || doc.isBlank())
            throw new ValidationException("identityDocument", Messages.DOC_REQUIRED);
        doc = doc.trim();
        if (!doc.matches("\\d{9,10}")) {
            throw new ValidationException("identityDocument", Messages.DOC_NUMERIC);
        };
        if (doc.length() < 6 || doc.length() > 20)
            throw new ValidationException("identityDocument", Messages.DOC_LENGTH);
        u.setIdentityDocument(doc);

        // Teléfono
        String phone = u.getPhoneNumber();
        if (phone == null || phone.isBlank())
            throw new ValidationException("phoneNumber", Messages.PHONE_REQUIRED);
        phone = phone.trim();
        if (!phone.matches("\\d+"))
            throw new ValidationException("phoneNumber", Messages.PHONE_NUMERIC);
        u.setPhoneNumber(phone);

        // Email
        String rawEmail = u.getEmail();
        if (rawEmail == null || rawEmail.isBlank())
            throw new ValidationException("email", Messages.EMAIL_REQUIRED);
        String emailNorm = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_RE.matcher(emailNorm).matches())
            throw new ValidationException("email", Messages.EMAIL_INVALID);
        u.setEmail(emailNorm);

        // Salario
        BigDecimal s = u.getBaseSalary();
        if (s == null)
            throw new ValidationException("baseSalary", Messages.SALARY_REQUIRED);
        if (s.scale() > 2)
            throw new ValidationException("baseSalary", Messages.SALARY_DECIMALS);
        if (s.compareTo(SALARIO_MIN) < 0 || s.compareTo(SALARIO_MAX) > 0)
            throw new ValidationException("baseSalary", Messages.SALARY_RANGE);
    }
}