package com.paladin.vod.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author TontoZhou
 * @since 2020/8/4
 */
@Getter
@Setter
@ApiModel
public class UploadFileDTO {

    @Id
    private String id;

    @ApiModelProperty("文件名称")
    @NotEmpty(message = "文件名称不能为空")
    private String fileName;

    @ApiModelProperty("客户端文件路径")
    @NotEmpty(message = "文件客户端地址不能为空")
    private String clientFilePath;

    @ApiModelProperty("文件大小")
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

}
