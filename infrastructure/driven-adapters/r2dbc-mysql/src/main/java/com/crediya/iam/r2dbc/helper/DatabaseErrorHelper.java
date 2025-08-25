package com.crediya.iam.r2dbc.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;

public final class DatabaseErrorHelper {

    private DatabaseErrorHelper() {}

    // Patrones comunes en MySQL/MariaDB
    private static final Pattern FK_COL_BACKTICK =
            Pattern.compile("FOREIGN\\s+KEY\\s*\\(`([^`]+)`\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FK_COL_PAREN =
            Pattern.compile("FOREIGN\\s+KEY\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_NAME =
            Pattern.compile("for key '([^']+)'", Pattern.CASE_INSENSITIVE); // Duplicate entry ... for key 'users.email_UNIQUE'

    /** Devuelve la columna de la FK si se puede extraer (e.g., "id_rol"). */
    public static String extractFkColumn(String message) {
        if (message == null) return null;
        String msg = message.toLowerCase(Locale.ROOT);

        // 1) FOREIGN KEY (`id_rol`)
        Matcher m1 = FK_COL_BACKTICK.matcher(msg);
        if (m1.find()) {
            return m1.group(1);
        }
        // 2) FOREIGN KEY (id_rol)  -> sin backticks
        Matcher m2 = FK_COL_PAREN.matcher(msg);
        if (m2.find()) {
            // tomar el primer token dentro del paréntesis
            String inside = m2.group(1).trim();
            // si viniera algo como "`id_rol`, `otra_col`", toma la primera
            String first = inside.split(",")[0].trim();
            return first.replace("`", "");
        }
        return null;
    }

    /** Devuelve el nombre del índice llaves únicas (p.ej. 'usuario.email_unique'). */
    public static String extractKeyName(String message) {
        if (message == null) return null;
        Matcher m = KEY_NAME.matcher(message);
        return m.find() ? m.group(1).toLowerCase(Locale.ROOT) : null;
    }

    /** Error de unicidad (MySQL 1062) por texto. */
    public static boolean isMySqlUnique(String message) {
        if (message == null) return false;
        String msg = message.toLowerCase(Locale.ROOT);
        return msg.contains("duplicate entry") || msg.contains("1062");
    }

    /** Error de FK (MySQL 1452/1451) por texto. */
    public static boolean isMySqlFk(String message) {
        if (message == null) return false;
        String msg = message.toLowerCase(Locale.ROOT);
        return msg.contains("foreign key constraint fails") || msg.contains("1452") || msg.contains("1451");
    }

    /** Desanida la causa raíz. */
    public static Throwable rootCause(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return c;
    }
}
