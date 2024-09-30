package com.wzf.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author WuZhongfei
 * @date 2024年09月29日 16:38
 */
public class ServiceTestFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(Filter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("这是ServiceTestFilter的init方法");
        String msg = "init的信息:";
        msg += "请求内容:"+filterConfig.getServletContext()+"\t 参数:"+filterConfig.getInitParameterNames();
        logger.info(msg);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("这是ServiceTestFilter的doFilter方法");

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
