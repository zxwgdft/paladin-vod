package com.paladin.framework.service;


import java.io.Serializable;

/**
 * 用户会话信息
 *
 * @author TontoZhou
 * @since 2018年1月29日
 */
public abstract class UserSession implements Serializable {

    /**
     * 用户ID，不能为空
     *
     * @return
     */
    public abstract String getUserId();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final UserSession that = (UserSession) o;
        return getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        String userId = getUserId();
        // Objects.hash(userId);
        return userId != null ? userId.hashCode() + 31 : 0;
    }


}
