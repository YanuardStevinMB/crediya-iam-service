package com.crediya.iam.usecase.shared;

import java.math.BigDecimal;
public final class Messages {
    private Messages() {}

    // Entidad / request
    public static final String USER_REQUIRED       = "El usuario es obligatorio";

    // password
    public static final String PASSWORD_REQUIRED       = "La contraseña es obligatoria";
    public static final String PASSWORD_VALIDATED       = "La contraseña debe tener mínimo 8 caracteres y contener letras y números";

    // Documento
    public static final String DOC_REQUIRED        = "El documento de identidad es obligatorio";
    public static final String DOC_NUMERIC         = "El documento solo puede tener dígitos (0-9)";
    public static final String DOC_LENGTH          = "El documento debe tener entre 6 y 20 dígitos";

    // Teléfono
    public static final String PHONE_REQUIRED      = "El número de teléfono es obligatorio";
    public static final String PHONE_NUMERIC       = "El número de teléfono debe contener solo dígitos";

    // Email
    public static final String EMAIL_REQUIRED      = "El correo electrónico es obligatorio";
    public static final String EMAIL_INVALID       = "El correo electrónico no tiene un formato válido";
    public static final String EMAIL_DUPLICATED    = "El correo electrónico ya está registrado";
    public static final String EMAIL_DIFERENT    = "El correo no pertenece al mismo registrado en la plataforma";

    // Salario
    public static final String SALARY_REQUIRED     = "El salario debe ser un valor numérico entre 0 y 15,000,000";
    public static final String SALARY_DECIMALS     = "El salario admite máximo 2 decimales";
    public static final String SALARY_RANGE        = "El salario debe estar entre 0 y 15,000,000";

    //Login
    public static final String INVALID_CREDENTIALS        = "Credenciales inválidas";
    public  static final String DOCUMENT_EMAIL = "Documento o email nulo";
    //role
    public static final String ROLE_NOT_FOUND   = "Rol no encontrado";
    //Usuarios
    public  static  final  String USERS_NOT_FOUNDS= "Usuarios no encontrados";
    public  static  final  String USERS_NOT_FOUND= "Usuarios no encontrado";
    public  static  final  String USERS_FOUND= "Usuarios recuperados";
    public  static  final String USER_EXIST ="Ya existe un usuario con el correo";
    public  static  final String USER_SAVED ="Usuario creado correctamente";
    public  static  final  String USER_VALIDATED_ERROR = "Validación fallida";
    public  static  final  String USER_ALREADY_EXIST = "El usuario existe";
    public  static  final  String USER_NOT_EXIST = "El usuario no existe";


}
