package com.minecraft.vo;

import com.minecraft.dto.response.MenuVO;
import lombok.Data;

import java.util.List;

/**
 * Web 端消费 ticket 后得到的完整登录凭据与权限快照。
 */
@Data
public class QrConsumeVO {

    private String token;

    private QrUserInfoVO userInfo;

    private List<String> roles;

    private List<String> permissions;

    private List<MenuVO> menus;

    private Integer dataScope;

    private Boolean superAdmin;
}
