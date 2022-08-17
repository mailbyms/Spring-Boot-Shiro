package com.gyjian.shiro.shiro;

import com.gyjian.shiro.entity.ShiroUser;
import com.gyjian.shiro.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomerRealm extends AuthorizingRealm {
    static final String REDIS_KEY_PREFIX = "MIKE:SHIRO_JWT_";

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Environment env;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("### doGetAuthorizationInfo");
        // 获取用户身份信息方式1
        String username = JwtUtil.getUsername(principalCollection.toString());
        // 获取用户身份信息方式2
        ShiroUser shiroUser = (ShiroUser) principalCollection.getPrimaryPrincipal();

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        List<String> pers = new ArrayList<>();
        // TODO 改为从数据库读取
        pers.add("user:add");
        pers.add("user:edit");

        for (String per : pers) {
            authorizationInfo.addStringPermission(per);
        }

        log.info("authorizationInfo:{}", authorizationInfo);
        return authorizationInfo;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        log.info("### doGetAuthenticationInfo");

        JwtToken jt = (JwtToken) authenticationToken;
        String jwtToken = jt.getToken();
        log.info("jwtToken:{}, source:{}", jwtToken, jt.getSource().name());

        // 从 redis 查找是否存在，如果 redis 缓存里有数据，则通过；否则查数据库
        ShiroUser shiroUser = (ShiroUser) redisUtil.get(REDIS_KEY_PREFIX + jwtToken);
        if (shiroUser != null) {
            log.info("redis 已存在 JwtToken 对应的实体，不需要数据库登录");
        } else {
            if (JwtToken.Source.FILTER.equals(jt.getSource())) {
                log.error("redis 找不到 JwtToken 对应的实体，返回失败");
                return null;
            }

            // 需要从数据库里查
            // 1. 获取客户端输入的身份信息，可以是名字、邮箱... 解密获得username，用于和数据库进行对比
            String username = JwtUtil.getUsername(jwtToken);

            log.info("username in token:{}", username);
            if (username == null) {
                System.out.println("解密出username 为空。token无效(空''或者null都不行!)");
                throw new AuthenticationException("token无效");
            }

            String pswFromDB = "123"; // TODO 改为从数据库查询获得

            // 检查密码是否正确
            if (!JwtUtil.verify(jwtToken, username, pswFromDB)) {
                System.out.println("用户名或密码错误(token无效或者与登录者不匹配)!");
                throw new AuthenticationException("用户名或密码错误(token无效或者与登录者不匹配)!");
            }

            shiroUser = new ShiroUser();
            shiroUser.setName(username);
            shiroUser.setId(new Random().nextInt());
            shiroUser.setRoleType((int) new Date().getTime());
        }

        int tokenTimeOut = Integer.valueOf(env.getProperty("token-time-out"));  // 从配置文件获取 redis 的超时设置
        log.info("设置 token 有效期为 {} 分钟", tokenTimeOut);
        redisUtil.set(REDIS_KEY_PREFIX + jwtToken, shiroUser, tokenTimeOut, TimeUnit.MINUTES);

        SimpleAuthenticationInfo simpleAuthenticationInfo =
                new SimpleAuthenticationInfo(shiroUser, jwtToken, getName());
        return simpleAuthenticationInfo;
    }

}
