package com.log.download.platform.dto;

import lombok.Data;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-01 14:54
 **/
@Data
public class FindMirrorDTO {
    //镜像的源ip
    private String ip;
    //镜像所在路径
    private String path;
}
