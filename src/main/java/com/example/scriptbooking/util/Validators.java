package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

public final class Validators {
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\- ]{6,30}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{4,20}$");
    private static final Pattern MOBILE = Pattern.compile("^1[3-9][0-9]{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");

    private Validators() {
    }

    public static String required(String value, String label, int maxLength) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty()) throw new BusinessException(label + "不能为空");
        if (result.length() > maxLength) throw new BusinessException(label + "不能超过" + maxLength + "个字符");
        return result;
    }

    public static String optional(String value, String label, int maxLength) {
        String result = value == null ? "" : value.trim();
        if (result.length() > maxLength) throw new BusinessException(label + "不能超过" + maxLength + "个字符");
        return result;
    }

    public static int positive(int value, String label) {
        if (value <= 0) throw new BusinessException(label + "必须大于0");
        return value;
    }

    public static int range(int value, int min, int max, String label) {
        if (value < min || value > max) throw new BusinessException(label + "必须在" + min + "到" + max + "之间");
        return value;
    }

    public static Integer optionalRating(Integer value, String label) {
        if (value != null) range(value, 1, 5, label);
        return value;
    }

    public static String oneOf(String value, Set<String> allowed, String label) {
        if (value == null || !allowed.contains(value)) throw new BusinessException(label + "不合法");
        return value;
    }

    public static String phone(String value) {
        String phone = required(value, "联系电话", 30);
        if (!PHONE.matcher(phone).matches()) throw new BusinessException("联系电话格式不正确");
        return phone;
    }

    public static void timeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
    }

    /** 注册用户名：4-20 位字母、数字或下划线。 */
    public static String username(String value) {
        String username = value == null ? "" : value.trim();
        if (username.isEmpty()) throw new BusinessException("用户名不能为空");
        if (!USERNAME.matcher(username).matches()) {
            throw new BusinessException("用户名需为 4-20 位字母、数字或下划线");
        }
        return username;
    }

    /** 注册邮箱：常规邮箱格式，最长 120 个字符。 */
    public static String email(String value) {
        String email = value == null ? "" : value.trim();
        if (email.isEmpty()) throw new BusinessException("邮箱不能为空");
        if (email.length() > 120) throw new BusinessException("邮箱不能超过120个字符");
        if (!EMAIL.matcher(email).matches()) throw new BusinessException("邮箱格式不正确");
        return email;
    }

    /** 注册手机号：11 位数字，以 1 开头，第二位为 3-9。 */
    public static String mobile(String value) {
        String mobile = value == null ? "" : value.trim();
        if (mobile.isEmpty()) throw new BusinessException("手机号不能为空");
        if (!MOBILE.matcher(mobile).matches()) {
            throw new BusinessException("手机号需为 11 位数字，以 1 开头且第二位为 3-9");
        }
        return mobile;
    }

    /** 注册密码：8-50 位，且同时包含字母和数字。密码不做 trim，首尾空格属于密码内容。 */
    public static String password(String value) {
        String password = value == null ? "" : value;
        if (password.isEmpty()) throw new BusinessException("密码不能为空");
        if (password.length() < 8 || password.length() > 50) {
            throw new BusinessException("密码长度需为 8-50 个字符");
        }
        if (!HAS_LETTER.matcher(password).matches() || !HAS_DIGIT.matcher(password).matches()) {
            throw new BusinessException("密码需同时包含字母和数字");
        }
        return password;
    }

    /** 两次输入的密码必须完全一致。 */
    public static void samePassword(String password, String confirmPassword) {
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new BusinessException("两次输入的密码不一致");
        }
    }
}
