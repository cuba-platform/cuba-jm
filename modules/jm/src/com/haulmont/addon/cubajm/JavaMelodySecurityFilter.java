package com.haulmont.addon.cubajm;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

public class JavaMelodySecurityFilter implements Filter {

    private String login;
    private String password;

    @Override
    public void init(FilterConfig filterConfig) {
        JavaMelodyConfig javaMelodyConfig = AppBeans.get(Configuration.class)
                .getConfig(JavaMelodyConfig.class);

        login = javaMelodyConfig.getAuthorizedUserLogin();
        password = javaMelodyConfig.getAuthorizedUserPassword();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _login = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();

                            if (!login.equals(_login) || !password.equals(_password)) {
                                unauthorized(httpResponse, "Bad credentials");
                            }

                            chain.doFilter(request, response);
                        } else {
                            unauthorized(httpResponse, "Invalid authentication token");
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }
            }
        } else {
            unauthorized(httpResponse);
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"Protected\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

    @Override
    public void destroy() {
    }
}