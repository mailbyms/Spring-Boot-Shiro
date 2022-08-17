
# Spring Boot Shiro

参考：  
https://blog.csdn.net/weixin_45390688/article/details/122148329  

## 重点
- Shiro 的重要概念 Realm（CustomerRealm.java），主要定义两个操作：认证（账号是否合法）和授权（账号拥有哪些权限） 
- ShiroConfig.java 为 shiro 的全局配置，定义哪些接口可以不拦截，哪些接口需要拦截；以及处理被拦截的接口的 Filter（MyFilter.java），被拦截的接口会进入 MyFilter 的 isAccessAllowed()  
- 因为是前后端分离的项目，不再有 session 的概念，ShiroConfig 要关闭 Shiro 自带的 Session 管理。即每个请求过来，都需要执行一次登录操作，给 Shiro 赋予保存对象（自定义的 ShiroUser 信息）
- 本项目的 JWT 只是信息传递的媒介，token 的一种形式。并没有用到 jwt 自带的有效期等特性（redis 控制有效期）

涉及2个主要流程：
1. Controller 的登录接口
```
封装 token 对象 -> SecurityUtils.getSubject().login(token)，最终调用 CustomerRealm 里的 doGetAuthenticationInfo()
```
2. Controller 的常规接口
```
MyFilter 拦截 -> isAccessAllowed() -> SecurityUtils.getSubject().login(token)，最终调用 CustomerRealm 里的 doGetAuthenticationInfo()  
```

doGetAuthenticationInfo() 根据调用的来源，可以做不同的处理流程：  
- 从 login 过来的请求，查数据库，如果用户名密码正确，保存一个 token 和 ShiroUser 对应信息到 Redis 缓存  
- 从 filter 过来的请求，先查 Redis 是否有对应缓存内容，有则通过并延长缓存有效期，无则返回 401 异常


## 测试，
代码里固定赋予了 "user:add" 和 "user:edit" 权限  
使用 Bearer Token

- 登录，会返回 token  
POST to `http://localhost:8080/user/login?username=mike&password=123`

- 权限测试接口1，要求 `user:add:*` 权限，通过  
POST to `http://localhost:8080/user/test`

- 权限测试接口2，要求 `user:del:*` 权限，返回异常  
POST to `http://localhost:8080/user/test2`
