package com.paladin.vod.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladin.framework.common.HttpCode;
import com.paladin.framework.exception.BusinessException;
import com.paladin.framework.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class VodHandlerExceptionResolver implements HandlerExceptionResolver {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            String errorMessage = "";
            HttpCode code = HttpCode.FAILURE;
            if (ex instanceof BusinessException) {
                errorMessage = ex.getMessage();
                log.debug("业务异常", ex);
            } else if (ex instanceof SystemException) {
                errorMessage = ex.getMessage();
                log.error("系统异常", ex);
            } else {
                errorMessage = "系统异常";
                log.error("其他异常", ex);
            }

            if (handlerMethod.getMethodAnnotation(ResponseBody.class) != null) {
                MappingJackson2JsonView jsonView = new MappingJackson2JsonView(objectMapper);
                jsonView.addStaticAttribute("message", errorMessage);
                jsonView.addStaticAttribute("success", false);
                jsonView.addStaticAttribute("code", code);
                return new ModelAndView(jsonView);
            } else {
                return new ModelAndView("/common/error/error", "errorMessage", errorMessage);
            }
        }

        return null;
    }

}
