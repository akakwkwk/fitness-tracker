package com.fitness.utils;

import com.fitness.common.BusinessException;
import com.fitness.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException(401, "未登录");
        }
        return ((UserPrincipal) auth.getPrincipal()).getUserId();
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException(401, "未登录");
        }
        return ((UserPrincipal) auth.getPrincipal()).getUsername();
    }
}
