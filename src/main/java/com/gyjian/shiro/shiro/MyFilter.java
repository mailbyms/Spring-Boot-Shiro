package com.gyjian.shiro.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class MyFilter extends BasicHttpAuthenticationFilter {
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        log.info("### executeLogin");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        log.info("executeLogin token:{}", authorization);
        String uri = ((HttpServletRequest) request).getRequestURI();
        log.info("请求路径:{}", uri);
        // 如果头部不存在
        if (StringUtils.isBlank(authorization)) {
            log.error("authorization 为空");
            return false;
        }

        String accessToken = authorization.replace("Bearer", "").trim();
        if (StringUtils.isBlank(accessToken)) {
            log.error("accessToken 为空");
            return false;
        }

        JwtToken jwtToken = new JwtToken(accessToken, JwtToken.Source.FILTER);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        try {
            getSubject(request, response).login(jwtToken);
            // 如果没有抛出异常则代表登入成功，返回true
            return true;
        } catch (AuthenticationException e) {
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            httpServletResponse.setCharacterEncoding("utf-8");
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().print("illegal access token");
            return false;
        }

    }

    /**
     * 执行登录认证
     * 返回 true，shiro就直接允许访问url
     * 返回 false，shiro才会根据onAccessDenied的方法的返回值决定是否允许访问
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        log.info("### isAccessAllowed");

        try {
            return executeLogin(request, response);
            // return true;有一篇博客这里直接返回true是不正确的,在这里特别指出一下
        } catch (Exception e) {
            e.printStackTrace();
            log.error("JwtFilter过滤认证失败!", e);
            return false;
        }
    }


    /**
     * 对跨域提供支持
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        log.info("### preHandle");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}

