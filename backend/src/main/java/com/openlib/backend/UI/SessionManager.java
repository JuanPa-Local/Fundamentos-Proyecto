package com.openlib.backend.UI;

/**
 * Almacena la sesión del usuario activo durante la ejecución de la aplicación.
 * No usa base de datos ni archivos; solo memoria de la JVM.
 */
public class SessionManager {

    private static String email    = "";
    private static String role     = "";
    private static String fullName = "";
    private static String token    = "";

    private SessionManager() {}

    public static String getEmail()    { return email; }
    public static String getRole()     { return role; }
    public static String getFullName() { return fullName; }
    public static String getToken()    { return token; }

    public static void setEmail(String e)    { email    = e != null ? e : ""; }
    public static void setRole(String r)     { role     = r != null ? r : ""; }
    public static void setFullName(String n) { fullName = n != null ? n : ""; }
    public static void setToken(String t)    { token    = t != null ? t : ""; }

    public static void clear() {
        email    = "";
        role     = "";
        fullName = "";
        token    = "";
    }

    public static boolean isLoggedIn() {
        return !email.isEmpty();
    }

    /** Header de autorización listo para usar en HttpRequest */
    public static String bearerHeader() {
        return token.isEmpty() ? "" : "Bearer " + token;
    }
}
