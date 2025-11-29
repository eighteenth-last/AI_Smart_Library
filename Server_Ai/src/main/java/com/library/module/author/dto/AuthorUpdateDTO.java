package com.library.module.author.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作者信息更新DTO
 */
@Data
public class AuthorUpdateDTO {

    /**
     * 作者名称
     */
    @Size(max = 100, message = "作者名称不能超过100字符")
    private String name;

    /**
     * 国家
     */
    @Size(max = 50, message = "国家名称不能超过50字符")
    private String country;

    /**
     * 出生年份
     */
    private Integer birthYear;

    /**
     * 作者简介
     */
    @Size(max = 1000, message = "作者简介不能超过1000字符")
    private String intro;
}
