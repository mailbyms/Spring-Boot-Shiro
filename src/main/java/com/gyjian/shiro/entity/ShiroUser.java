package com.gyjian.shiro.entity;

import lombok.Data;

@Data
public class ShiroUser {
	private Integer id;//人员id
    private String name;//姓名
    private String roleName; //  角色名称
    private Integer roleType; //类型：0,普通人员；1,厂管理员；2,市管理员；3,区管理员；4,部门管理员；5,班管理员；6,自定义管理员；100,初始管理员
}
