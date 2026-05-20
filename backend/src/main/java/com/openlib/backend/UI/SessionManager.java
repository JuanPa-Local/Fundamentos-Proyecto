package com.openlib.backend.UI;

/**
 * Almacena la sesión del usuario activo durante la ejecución de la aplicación.
 * Implementa el patrón Singleton.
 */
public class SessionManager {

    private static SessionManager instance;

    private String email    = "";
    private String role     = "";
    private String fullName = "";
    private String token    = "";

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getEmail()    { return email; }
    public String getRole()     { return role; }
    public String getFullName() { return fullName; }
    public String getToken()    { return token; }

    public void setEmail(String e)    { email    = e != null ? e : ""; }
    public void setRole(String r)     { role     = r != null ? r : ""; }
    public void setFullName(String n) { fullName = n != null ? n : ""; }
    public void setToken(String t)    { token    = t != null ? t : ""; }

    public void clear() {
        email    = "";
        role     = "";
        fullName = "";
        token    = "";
    }

    public boolean isLoggedIn() {
        return !email.isEmpty();
    }

    /** Header de autorización listo para usar en HttpRequest */
    public String bearerHeader() {
        return token.isEmpty() ? "" : "Bearer " + token;
    }
}
