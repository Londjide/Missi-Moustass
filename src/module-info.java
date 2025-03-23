/**
 * Module principal pour l'application Missié Moustass
 * Application de gestion sécurisée de messages vocaux
 */
module Missié_Moustass_V1 {
    requires java.sql;
    requires java.desktop;
    requires java.security.jgss;
    
    exports com.barbichetz.voice;
    exports com.barbichetz.security;
    exports com.barbichetz.storage;
}