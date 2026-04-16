package services;

import Klaseak.Langilea;

public class SessionContext {

    private static String currentUser;
    private static Langilea currentLangilea;
    private static Boolean currentUserTxatSarbidea;

    private SessionContext() {}

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static void setCurrentLangilea(Langilea langilea) {
        currentLangilea = langilea;
        currentUserTxatSarbidea = langilea != null ? langilea.isTxatSarbidea() : null;
    }

    public static String getCurrentUser() {
        return currentUser != null ? currentUser : "EZEZAGUNA";
    }

    public static Langilea getCurrentLangilea() {
        return currentLangilea;
    }

    public static boolean isChatAllowed() {
        return currentUserTxatSarbidea == null || currentUserTxatSarbidea;
    }

    public static void clear() {
        currentUser = null;
        currentLangilea = null;
        currentUserTxatSarbidea = null;
    }
}

