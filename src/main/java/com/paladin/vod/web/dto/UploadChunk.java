package com.paladin.vod.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author TontoZhou
 * @since 2020/8/6
 */
@Getter
@Setter
public class UploadChunk {

    private String id;
    private int chunk;
    private MultipartFile file;

}
