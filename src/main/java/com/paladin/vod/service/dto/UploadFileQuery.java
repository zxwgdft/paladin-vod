package com.paladin.vod.service.dto;

import com.paladin.framework.service.OffsetPage;
import com.paladin.framework.service.QueryCondition;
import com.paladin.framework.service.QueryType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author TontoZhou
 * @since 2020/8/4
 */
@Getter
@Setter
public class UploadFileQuery extends OffsetPage {

    @QueryCondition(type = QueryType.LIKE)
    private String fileName;

    @QueryCondition(type = QueryType.EQUAL)
    private int status;

}