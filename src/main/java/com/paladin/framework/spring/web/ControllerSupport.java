package com.paladin.framework.spring.web;

import com.paladin.framework.common.HttpCode;
import com.paladin.framework.common.R;
import com.paladin.framework.utils.convert.SimpleBeanCopyUtil;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ControllerSupport {

    /**
     * 验证异常处理
     *
     * @param bindingResult
     * @return
     */
    public R validErrorHandler(BindingResult bindingResult) {

        List<FieldError> errors = bindingResult.getFieldErrors();

        String[][] result = new String[errors.size()][3];

        int i = 0;

        for (FieldError error : bindingResult.getFieldErrors()) {
            result[i++] = new String[]{error.getCode(), error.getField(), error.getDefaultMessage()};
        }

        return R.fail(HttpCode.BAD_REQUEST, "请求参数验证未通过", result);
    }

    protected <T> T beanCopy(Object source, T target) {
        return SimpleBeanCopyUtil.simpleCopy(source, target);
    }

    public <T> List<T> beanCopyList(List<?> sourceList, Class<T> targetType) {
        return SimpleBeanCopyUtil.simpleCopyList(sourceList, targetType);
    }

    public <T> List<T> beanCopyList(List<?> sourceList, List<T> targeList) {
        return SimpleBeanCopyUtil.simpleCopyList(sourceList, targeList);
    }

}
