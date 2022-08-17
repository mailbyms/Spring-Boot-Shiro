package com.gyjian.shiro.controller;

import com.gyjian.shiro.shiro.JwtToken;
import com.gyjian.shiro.shiro.JwtUtil;
import com.gyjian.shiro.entity.ShiroUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(String username, String password, HttpServletResponse response) throws Exception {
        String tokenStr = JwtUtil.sign(username, password);
        JwtToken token = new JwtToken(tokenStr, JwtToken.Source.LOGIN);
        Subject subject = SecurityUtils.getSubject();

        subject.login(token);

        return ResponseEntity.ok(tokenStr);
    }

    @RequestMapping("/test")
    @ResponseBody
    @RequiresPermissions("user:add:*")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("success");
    }

    @RequestMapping("/test2")
    @ResponseBody
    @RequiresPermissions("user:del:*")
    public ResponseEntity<ShiroUser> delete() {
        Subject subject = SecurityUtils.getSubject();
        ShiroUser shiroUser = (ShiroUser) subject.getPrincipal();

        return ResponseEntity.ok(shiroUser);
    }
}

