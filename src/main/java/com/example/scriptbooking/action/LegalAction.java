package com.example.scriptbooking.action;

/**
 * 注册页引用的静态说明页面：用户协议与隐私政策。
 */
public class LegalAction extends BaseAction {

    public String terms() {
        setPageTitle("用户协议");
        return SUCCESS;
    }

    public String privacy() {
        setPageTitle("隐私政策");
        return SUCCESS;
    }
}
