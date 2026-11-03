package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注册表单的服务端校验规则测试，覆盖用户名、11 位手机号、密码强度与两次密码一致性。
 * 与前端 assets/js/register.js 中的实时校验规则保持一致。
 */
class RegistrationValidationTest {

    @Test
    void acceptsValidRegistrationInput() {
        assertEquals("player_09", Validators.username("player_09"));
        assertEquals("player_09", Validators.username("  player_09  "));
        assertEquals("13800001004", Validators.mobile("13800001004"));
        assertEquals("13800001004", Validators.mobile(" 13800001004 "));
        assertEquals("abc12345", Validators.password("abc12345"));
        assertDoesNotThrow(() -> Validators.samePassword("abc12345", "abc12345"));
    }

    @Test
    void acceptsAllCommonMobilePrefixes() {
        String[] mobiles = {"13000000000", "14500000000", "15900000000", "17800000000", "18800000000", "19900000000"};
        for (String mobile : mobiles) {
            assertEquals(mobile, Validators.mobile(mobile));
        }
    }

    @Test
    void rejectsInvalidUsername() {
        assertThrows(BusinessException.class, () -> Validators.username(null));
        assertThrows(BusinessException.class, () -> Validators.username("  "));
        assertThrows(BusinessException.class, () -> Validators.username("abc"));
        assertThrows(BusinessException.class, () -> Validators.username("this_name_is_too_long_for_the_limit"));
        assertThrows(BusinessException.class, () -> Validators.username("bad name"));
        assertThrows(BusinessException.class, () -> Validators.username("中文账号"));
    }

    @Test
    void rejectsPhoneThatIsNotElevenDigits() {
        assertThrows(BusinessException.class, () -> Validators.mobile(null));
        assertThrows(BusinessException.class, () -> Validators.mobile(""));
        assertThrows(BusinessException.class, () -> Validators.mobile("   "));
        assertThrows(BusinessException.class, () -> Validators.mobile("1380000100"));
        assertThrows(BusinessException.class, () -> Validators.mobile("138000010041"));
    }

    @Test
    void rejectsPhoneWithWrongFormat() {
        assertThrows(BusinessException.class, () -> Validators.mobile("12800001004"));
        assertThrows(BusinessException.class, () -> Validators.mobile("23800001004"));
        assertThrows(BusinessException.class, () -> Validators.mobile("1380000100a"));
        assertThrows(BusinessException.class, () -> Validators.mobile("138 0000 1004"));
        assertThrows(BusinessException.class, () -> Validators.mobile("138-0000-1004"));
    }

    @Test
    void rejectsWeakOrInvalidPassword() {
        assertThrows(BusinessException.class, () -> Validators.password(null));
        assertThrows(BusinessException.class, () -> Validators.password("abc123"));
        assertThrows(BusinessException.class, () -> Validators.password("abcdefgh"));
        assertThrows(BusinessException.class, () -> Validators.password("12345678"));
        assertThrows(BusinessException.class, () -> Validators.password(repeat('a', 51)));
    }

    @Test
    void rejectsPasswordMismatch() {
        assertThrows(BusinessException.class, () -> Validators.samePassword("abc12345", "abc123456"));
        assertThrows(BusinessException.class, () -> Validators.samePassword("abc12345", null));
    }

    private String repeat(char value, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) builder.append(value);
        return builder.toString();
    }
}
