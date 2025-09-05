package com.crediya.iam.usecase.existuser;
import com.crediya.iam.usecase.shared.Messages;
import com.crediya.iam.usecase.shared.ValidationException;
import lombok.RequiredArgsConstructor;


public class ExistUserValidated {
    private  ExistUserValidated(){}
    public static void validateAndNormalize(String document) {
        // Documento
        String doc = document;
        if (doc == null || doc.isBlank())
            throw new ValidationException("identityDocument", Messages.DOC_REQUIRED);
        doc = doc.trim();
        if (!doc.matches("\\d{9,10}")) {
            throw new ValidationException("identityDocument", Messages.DOC_NUMERIC);
        };
        if (doc.length() < 6 || doc.length() > 20)
            throw new ValidationException("identityDocument", Messages.DOC_LENGTH);
        };
    }
