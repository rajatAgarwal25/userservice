package com.proptiger.userservice.config.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.session.Session;
import org.springframework.session.web.http.HttpSessionManager;
import org.springframework.session.web.http.MultiHttpSessionStrategy;

/**
 * @author Rajeev Pandey
 *
 */
public class CustomCookieHttpSessionStrategy  implements MultiHttpSessionStrategy, HttpSessionManager {

    static final String DEFAULT_ALIAS = "0";

    static final String DEFAULT_SESSION_ALIAS_PARAM_NAME = "_s";

    private String cookieName = "SESSION";

    private String sessionParam = DEFAULT_SESSION_ALIAS_PARAM_NAME;

    @Override
    public String getRequestedSessionId(HttpServletRequest request) {
        Map<String,String> sessionIds = getSessionIds(request);
        String sessionAlias = getCurrentSessionAlias(request);
        return sessionIds.get(sessionAlias);
    }

    @Override
    public String getCurrentSessionAlias(HttpServletRequest request) {
        if(sessionParam == null) {
            return DEFAULT_ALIAS;
        }
        String u = request.getParameter(sessionParam);
        if(u == null) {
            return DEFAULT_ALIAS;
        }
        return u;
    }

    @Override
    public void onNewSession(Session session, HttpServletRequest request, HttpServletResponse response) {
        Map<String,String> sessionIds = getSessionIds(request);
        String sessionAlias = getCurrentSessionAlias(request);
        sessionIds.put(sessionAlias, session.getId());
        Cookie sessionCookie = createSessionCookie(request, sessionIds);
        response.addCookie(sessionCookie);
    }

    private Cookie createSessionCookie(HttpServletRequest request,
            Map<String, String> sessionIds) {
        Cookie sessionCookie = new Cookie(cookieName,"");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(request.isSecure());
        sessionCookie.setPath(cookiePath(request));
        // TODO set domain?

        if(sessionIds.isEmpty()) {
            sessionCookie.setMaxAge(0);
            return sessionCookie;
        }

        if(sessionIds.size() == 1) {
            String cookieValue = sessionIds.values().iterator().next();
            sessionCookie.setValue(cookieValue);
            return sessionCookie;
        }
        StringBuffer buffer = new StringBuffer();
        for(Map.Entry<String,String> entry : sessionIds.entrySet()) {
            String alias = entry.getKey();
            String id = entry.getValue();

            buffer.append(alias);
            buffer.append(" ");
            buffer.append(id);
            buffer.append(" ");
        }
        buffer.deleteCharAt(buffer.length()-1);

        sessionCookie.setValue(buffer.toString());
        return sessionCookie;
    }

    @Override
    public void onInvalidateSession(HttpServletRequest request, HttpServletResponse response) {
        Map<String,String> sessionIds = getSessionIds(request);
        String requestedAlias = getCurrentSessionAlias(request);
        sessionIds.remove(requestedAlias);

        Cookie sessionCookie = createSessionCookie(request, sessionIds);
        response.addCookie(sessionCookie);
    }

    /**
     * Sets the name of the HTTP parameter that is used to specify the session
     * alias. If the value is null, then only a single session is supported per
     * browser.
     *
     * @param sessionAliasParamName
     *            the name of the HTTP parameter used to specify the session
     *            alias. If null, then ony a single session is supported per
     *            browser.
     */
    public void setSessionAliasParamName(String sessionAliasParamName) {
        this.sessionParam = sessionAliasParamName;
    }

    /**
     * Sets the name of the cookie to be used
     * @param cookieName
     */
    public void setCookieName(String cookieName) {
        if(cookieName == null) {
            throw new IllegalArgumentException("cookieName cannot be null");
        }
        this.cookieName = cookieName;
    }

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     * @param request current servlet request
     * @param name cookie name
     * @return the first cookie with the given name, or {@code null} if none is found
     */
    private static Cookie getCookie(HttpServletRequest request, String name) {
        if(request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private static String cookiePath(HttpServletRequest request) {
        return /*request.getContextPath() +*/ "/";
    }

    @Override
    public Map<String,String> getSessionIds(HttpServletRequest request) {
        Cookie session = getCookie(request, cookieName);
        String sessionCookieValue = session == null ? "" : session.getValue();
        Map<String,String> result = new LinkedHashMap<String,String>();
        StringTokenizer tokens = new StringTokenizer(sessionCookieValue, " ");
        if(tokens.countTokens() == 1) {
            result.put(DEFAULT_ALIAS, tokens.nextToken());
            return result;
        }
        while(tokens.hasMoreTokens()) {
            String alias = tokens.nextToken();
            String id = tokens.nextToken();
            result.put(alias, id);
        }
        return result;
    }

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute(HttpSessionManager.class.getName(), this);
        return request;
    }

    @Override
    public HttpServletResponse wrapResponse(HttpServletRequest request, HttpServletResponse response) {
        return new MultiSessionHttpServletResponse(response, request);
    }

    class MultiSessionHttpServletResponse extends HttpServletResponseWrapper {
        private final HttpServletRequest request;

        public MultiSessionHttpServletResponse(HttpServletResponse response, HttpServletRequest request) {
            super(response);
            this.request = request;
        }

        @Override
        public String encodeRedirectURL(String url) {
            url = super.encodeRedirectURL(url);
            return CustomCookieHttpSessionStrategy.this.encodeURL(url, getCurrentSessionAlias(request));
        }

        @Override
        public String encodeURL(String url) {
            url = super.encodeURL(url);

            String alias = getCurrentSessionAlias(request);
            return CustomCookieHttpSessionStrategy.this.encodeURL(url, alias);
        }
    }

    @Override
    public String encodeURL(String url, String sessionAlias) {
        int queryStart = url.indexOf("?");
        boolean isDefaultAlias = DEFAULT_ALIAS.equals(sessionAlias);
        if(queryStart < 0) {
            return isDefaultAlias ? url : url + "?" + sessionParam + "=" + sessionAlias;
        }
        String path = url.substring(0, queryStart);
        String query = url.substring(queryStart + 1, url.length());
        String replacement = isDefaultAlias ? "" : "$1"+sessionAlias;
        query = query.replaceFirst( "((^|&)" + sessionParam + "=)([^&]+)?", replacement);
        if(!isDefaultAlias && url.endsWith(query)) {
            // no existing alias
            if(!(query.endsWith("&") || query.length() == 0)) {
                query += "&";
            }
            query += sessionParam + "=" + sessionAlias;
        }

        return path + "?" + query;
    }

}
