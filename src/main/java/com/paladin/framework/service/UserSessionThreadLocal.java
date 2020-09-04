package com.paladin.framework.service;

/**
 * @author TontoZhou
 * @since 2020/1/2
 */
public class UserSessionThreadLocal {

    private UserSessionFactory userSessionFactory;

    public UserSessionThreadLocal(UserSessionFactory userSessionFactory) {
        this.userSessionFactory = userSessionFactory;
    }

    private final static ThreadLocal<UserSession> sessionMap = new ThreadLocal<>();

    public UserSession createUserSession(String userId) {
        if (userId != null && userId.length() > 0) {
            UserSession userSession = userSessionFactory.createUserSession(userId);
            sessionMap.set(userSession);
            return userSession;
        } else {
            sessionMap.set(null);
        }
        return null;
    }

    public static UserSession getCurrentUserSession() {
        return sessionMap.get();
    }
}
