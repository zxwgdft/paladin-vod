package com.paladin.vod.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author TontoZhou
 * @since 2020/8/6
 */
@Getter
@Setter
public class UploadStatus {

    private String id;
    private long chunkSize;
    private int currentChunk;

    private int status;

}
