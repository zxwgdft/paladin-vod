package com.paladin.vod.config;

import com.paladin.framework.jwt.SHATokenProvider;
import com.paladin.framework.jwt.TokenProvider;
import com.paladin.framework.service.DataContainerManager;
import com.paladin.framework.service.ServiceSupportManager;
import com.paladin.framework.spring.SpringBeanHelper;
import com.paladin.framework.spring.SpringContainerManager;
import com.paladin.framework.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * @author TontoZhou
 * @since 2019/12/10
 */
@Slf4j
@Configuration
public class VodConfiguration {

    @Bean
    public TokenProvider getTokenProvider(Environment env) {
        SHATokenProvider tokenProvider = new SHATokenProvider();
        tokenProvider.setBase64Key(env.getProperty("jwt.key"));
        String expireMillisecondStr = env.getProperty("jwt.expire-millisecond");
        long expireMillisecond = StringUtil.isEmpty(expireMillisecondStr) ? 30 * 60 * 1000 : Long.valueOf(expireMillisecondStr);
        tokenProvider.setTokenExpireMilliseconds(expireMillisecond);
        tokenProvider.setIssuer(env.getProperty("jwt.issuer"));
        return tokenProvider;
    }

    /**
     * 启用异常统一处理
     *
     * @return
     */
    @Bean
    public HandlerExceptionResolver getHandlerExceptionResolver() {
        return new VodHandlerExceptionResolver();
    }

    /**
     * 数据容器管理器
     *
     * @return
     */
    @Bean
    public DataContainerManager getDataContainerManager() {
        return new DataContainerManager();
    }

    /**
     * spring bean 获取帮助类
     */
    @Bean
    public SpringBeanHelper springBeanHolder() {
        return new SpringBeanHelper();
    }


    /**
     * service支持管理器
     */
    @Bean
    public ServiceSupportManager getServiceSupportContainer() {
        return new ServiceSupportManager();
    }


    @Bean
    public SpringContainerManager springContainerManager() {
        return new SpringContainerManager();
    }
}
