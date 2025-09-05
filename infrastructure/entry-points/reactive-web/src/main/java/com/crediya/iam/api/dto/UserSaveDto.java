package com.crediya.iam.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiResponseUserResponseDto")

public record UserSaveDto(

        Long id, // opcional, no validar

        @NotBlank(message = "El campo 'firstName' es obligatorio.")
        @Size(max = 150, message = "firstName no debe exceder 150 caracteres.")
        String firstName,

        @NotBlank(message = "El campo 'lastName' es obligatorio.")
        @Size(max = 150, message = "lastName no debe exceder 150 caracteres.")
        String lastName,

        @NotBlank(message = "El campo 'email' es obligatorio.")
        @Email(message = "El formato de 'email' no es válido.")
        @Size(max = 180, message = "email no debe exceder 180 caracteres.")
        String email,

        // La fecha viaja como String ISO-8601
        @NotNull(message = "El campo 'birthdate' es obligatorio.")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
        LocalDate birthdate,

        @NotBlank(message = "El campo 'identityDocument' es obligatorio.")
        @Size(max = 50, message = "identityDocument no debe exceder 50 caracteres.")
        String identityDocument,

        @NotBlank(message = "El campo 'phoneNumber' es obligatorio.")
        @Pattern(
                regexp = "^[0-9+()\\-\\s]{7,20}$",
                message = "phoneNumber contiene caracteres inválidos o longitud incorrecta."
        )
        String phoneNumber,

        @NotNull(message = "El campo 'baseSalary' es obligatorio.")
        @DecimalMin(value = "0.0", inclusive = true, message = "baseSalary debe ser >= 0.")
        @DecimalMax(value = "15000000.0", inclusive = true, message = "baseSalary no debe superar 15,000,000.")
        @Digits(integer = 8, fraction = 2, message = "baseSalary debe tener hasta 8 enteros y 2 decimales.")
        BigDecimal baseSalary,

        @NotBlank(message = "El campo 'address' es obligatorio.")
        String address,

        @NotBlank(message = "El campo 'password' es obligatorio.")
        String password,

        @NotNull(message = "El campo 'roleId' es obligatorio.")
        Long roleId
) {}
