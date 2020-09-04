package com.paladin.framework.service;

/**
 * @author TontoZhou
 * @since 2020/1/2
 */
public interface UserSessionFactory {

    UserSession createUserSession(String subject);

}
