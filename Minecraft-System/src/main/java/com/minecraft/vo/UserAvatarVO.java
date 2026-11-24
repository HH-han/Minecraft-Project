package com.minecraft.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开头像查询响应。
 * <p>只暴露头像字段，不返回账号、手机号等任何其他用户信息，避免信息泄漏。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAvatarVO {
    /** 头像地址；账号不存在、无头像或参数非法时为空字符串 */
    private String avatar;
}
