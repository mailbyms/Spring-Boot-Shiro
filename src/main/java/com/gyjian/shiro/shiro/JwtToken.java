package com.gyjian.shiro.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

public class JwtToken extends UsernamePasswordToken {
    public enum Source {
        LOGIN, FILTER
    }

    private String token;
    private Source source; // 来源：1 从登录接口过来  2 从普通接口访问的 filter 过来

    public JwtToken(String token, Source source) {
        this.token = token;
        this.source = source;
    }

    public Source getSource() {
        return source;
    }

    public String getToken() {
        return token;
    }

    /**
     * 重写 getCredentials 函数，以便 CustomRealm 的 doGetAuthenticationInfo() 最后返回时的校验
     * @return
     */
    @Override
    public Object getCredentials() {
        return token;
    }
}
