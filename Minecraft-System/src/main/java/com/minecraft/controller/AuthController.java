package com.minecraft.controller;

import com.minecraft.dto.request.EmailCaptchaRequest;
import com.minecraft.dto.request.LoginRequest;
import com.minecraft.dto.request.RegisterRequest;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.LoginResponse;
import com.minecraft.dto.response.MenuVO;
import com.minecraft.entity.User;
import com.minecraft.exception.BusinessException;
import com.minecraft.security.core.LoginUser;
import com.minecraft.service.UserPermissionService;
import com.minecraft.service.UserService;
import com.minecraft.utils.SecurityUtils;
import com.minecraft.vo.UserMeVO;
import com.minecraft.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPermissionService userPermissionService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = userService.login(request, httpRequest);
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            String account = userService.register(request);
            return ApiResponse.success("注册成功", account);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email-captcha")
    public ApiResponse<String> sendEmailCaptcha(@Valid @RequestBody EmailCaptchaRequest request) {
        try {
            userService.sendEmailCaptcha(request.getEmail());
            return ApiResponse.success("验证码发送成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/codes")
    public ApiResponse<User> getUserInfo() {
        Long userId = requireLogin();
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "当前登录用户信息 + 权限码")
    @GetMapping("/me")
    public ApiResponse<UserMeVO> me() {
        LoginUser loginUser = requireLoginUser();
        User user = userService.getUserInfo(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在，请重新登录");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoles(List.copyOf(loginUser.getRoleCodes()));
        userVO.setSuperAdmin(loginUser.isSuperAdmin());

        UserMeVO me = new UserMeVO();
        me.setUserInfo(userVO);
        me.setRoles(loginUser.getRoleCodes());
        me.setPermissions(loginUser.getPermissions());
        me.setDataScope(loginUser.getDataScope());
        me.setSuperAdmin(loginUser.isSuperAdmin());
        return ApiResponse.success(me);
    }

    @Operation(summary = "当前用户权限码列表")
    @GetMapping("/permissions")
    public ApiResponse<List<String>> permissions() {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.success(List.copyOf(loginUser.getPermissions()));
    }

    @Operation(summary = "当前用户可见菜单树")
    @GetMapping("/menus")
    public ApiResponse<List<MenuVO>> menus() {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.success(userPermissionService.getMenus(loginUser.getUserId()));
    }

    @Operation(summary = "退出登录（清理权限缓存）")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        Long userId = requireLogin();
        userPermissionService.evictUser(userId);
        userService.updateOnlineStatus(userId, false);
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "修改自身密码")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Long userId = requireLogin();
        userService.updatePassword(userId, oldPassword, newPassword);
        userPermissionService.evictUser(userId);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/email-login")
    public ApiResponse<LoginResponse> loginByEmail(@Valid @RequestBody EmailCaptchaRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = userService.loginByEmail(request.getEmail(), request.getCaptcha(), httpRequest);
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private Long requireLogin() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "请先登录");
        }
        return loginUser;
    }
}
