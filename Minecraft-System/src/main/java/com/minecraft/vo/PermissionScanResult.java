package com.minecraft.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口权限扫描结果（开发辅助）。
 */
@Data
public class PermissionScanResult {

    /** 代码中已标注、但数据库中不存在的权限码 */
    private List<String> missingInDb = new ArrayList<>();

    /** 数据库中存在、但代码中已不再使用的权限码 */
    private List<String> missingInCode = new ArrayList<>();

    /** 管理端未标注 @PreAuthorize 的接口 */
    private List<String> unprotected = new ArrayList<>();
}
