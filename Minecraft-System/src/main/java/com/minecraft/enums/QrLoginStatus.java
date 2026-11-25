package com.minecraft.enums;

/**
 * 扫码登录 ticket 状态。
 *
 * <p>状态只能单向前进：WAITING → SCANNED → CONFIRMED → CONSUMED，
 * EXPIRED / CANCELED 为终态。</p>
 */
public enum QrLoginStatus {

    /** 等待 APP 扫码 */
    WAITING("等待扫码"),

    /** 已被 APP 扫描，等待 APP 确认 */
    SCANNED("已扫描"),

    /** APP 已确认，等待 Web 消费 */
    CONFIRMED("已确认"),

    /** Web 已消费（ticket 已删除） */
    CONSUMED("已使用"),

    /** 二维码过期 */
    EXPIRED("已过期"),

    /** APP 主动取消 */
    CANCELED("已取消");

    private final String description;

    QrLoginStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == CONSUMED || this == EXPIRED || this == CANCELED;
    }
}
