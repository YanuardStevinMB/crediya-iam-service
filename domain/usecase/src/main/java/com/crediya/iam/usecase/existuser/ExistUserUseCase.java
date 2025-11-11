package com.crediya.iam.usecase.existuser;

import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.Messages;
import com.crediya.iam.usecase.shared.security.ExceptionGeneral;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class ExistUserUseCase {
    private static final Logger LOG = Logger.getLogger(ExistUserUseCase.class.getName());
    private final UserRepository userRepository;


    public Mono<Boolean> execute(String document, String email) {
        if (document == null || email == null) {
            LOG.warning(Messages.DOCUMENT_EMAIL);
            return Mono.error(new ExceptionGeneral(Messages.DOCUMENT_EMAIL));
        }

        return userRepository.existUserForDocument(document)
                .flatMap(user -> {
                    LOG.info(String.format("[ExistUserUseCase] user found: id=%s document=%s email=%s",
                            user.getId(), user.getIdentityDocument(), user.getEmail()));

                    String storedEmail = user.getEmail() != null ? user.getEmail().trim() : "";
                    String inputEmail = email.trim();

                    if (storedEmail.equalsIgnoreCase(inputEmail)) {
                        LOG.info("[ExistUserUseCase] email match, returning true ");
                        return Mono.just(true);
                    } else {
                        LOG.warning(String.format(
                                "[ExistUserUseCase] email mismatch : input=%s stored=%s",
                                inputEmail, storedEmail));
                        return Mono.error(new IllegalArgumentException(Messages.EMAIL_DIFERENT
                        ));
                    }
                })
                .switchIfEmpty(Mono.error(new ExceptionGeneral(Messages.USERS_NOT_FOUND)));
    }
}
