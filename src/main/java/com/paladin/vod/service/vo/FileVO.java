package com.paladin.vod.service.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileVO {

    private String name;
    private String relativePath;
    private long lastUpdateTime;
    private long size;

}
