package com.paladin.vod.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import java.util.Date;

/**
 * @author TontoZhou
 * @since 2020/8/4
 */
@Getter
@Setter
@ApiModel
public class UploadFileVO {

    @Id
    private String id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("客户端文件路径")
    private String clientFilePath;

    @ApiModelProperty("服务端相对路径")
    private String serverRelativePath;

    @ApiModelProperty("后缀")
    private String suffix;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("块大小")
    private Integer chunkSize;

    @ApiModelProperty("完成块数")
    private Integer finishChunk;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("转码状态")
    private Integer transcodeStatus;


}

