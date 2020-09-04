package com.paladin.vod.config;

import com.paladin.framework.common.R;
import com.paladin.framework.exception.BusinessException;
import com.paladin.framework.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

/**
 * @author TontoZhou
 * @since 2019/12/11
 */
@Slf4j
@ControllerAdvice
public class VodExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> businessExceptionHandler(BusinessException ex, WebRequest request) {
        return handleExceptionInternal(ex, R.fail(ex.getMessage(), ex.getData()), new HttpHeaders(), ex.getHttpStatus(), request);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Object> systemExceptionHandler(SystemException ex, WebRequest request) {
        log.error("系统异常！", ex);
        return handleExceptionInternal(ex, R.fail(ex.getMessage()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> systemExceptionHandler(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, R.fail(ex.getMessage()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        log.error("发生异常", ex);

        if (body == null) {
            body = R.fail(ex.getMessage());
        }

        if (!(body instanceof R)) {
            body = R.fail(ex.getMessage(), body);
        }

        return new ResponseEntity<>(body, headers, status);
    }

}
