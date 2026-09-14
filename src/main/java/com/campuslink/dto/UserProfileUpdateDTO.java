package com.campuslink.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserProfileUpdateDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickName;

    @Size(max = 100, message = "简介不能超过100个字符")
    private String introduce;
}
