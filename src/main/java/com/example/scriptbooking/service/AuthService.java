package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.UserDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Validators;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collections;
import java.util.LinkedHashSet;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User authenticate(String username, String password) {
        String account = Validators.required(username, "账号", 50);
        String secret = Validators.required(password, "密码", 200);
        User user = userDao.findByUsername(account).orElseThrow(() -> new BusinessException("账号或密码错误"));
        if (!"ACTIVE".equals(user.getStatus())) throw new BusinessException("账号已被禁用");
        if (!BCrypt.checkpw(secret, user.getPasswordHash())) throw new BusinessException("账号或密码错误");
        return user;
    }

    /**
     * 注册新玩家账号：校验格式与唯一性后写入 sys_user 并授予 PLAYER 角色。
     * 手机号要求 11 位；用户名、手机号的唯一性还会由数据库唯一索引兜底，防止并发重复注册。
     */
    public User register(String username, String phone, String password, String confirmPassword) {
        String account = Validators.username(username);
        String mobile = Validators.mobile(phone);
        String secret = Validators.password(password);
        Validators.samePassword(secret, confirmPassword);

        if (userDao.findByUsername(account).isPresent()) {
            throw new BusinessException("该用户名已被注册");
        }
        if (userDao.existsByPhone(mobile)) {
            throw new BusinessException("该手机号已被注册");
        }

        User user = new User();
        user.setUsername(account);
        user.setPhone(mobile);
        user.setDisplayName(account);
        user.setStatus("ACTIVE");
        user.setPasswordHash(BCrypt.hashpw(secret, BCrypt.gensalt(10)));
        user.setRoles(new LinkedHashSet<>(Collections.singletonList("PLAYER")));
        user.setId(userDao.insert(user));
        return user;
    }
}
