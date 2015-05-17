package dnss.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class Cookies {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HashMap<String, Cookie> map = new HashMap<>();
    final public static String DEFAULT_PATH = "/";
    final public static int DEFAULT_MAX_AGE = 31556926;


    public Cookies(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public Cookie get(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }

        return null;
    }

    public void set(String name, Cookie value) {
        map.put(name, value);
    }

    public void create(String name, Object value) {
        create(name, value, DEFAULT_MAX_AGE, DEFAULT_PATH);
    }

    public void create(String name, Object value, int maxAge) {
        create(name, value, maxAge, DEFAULT_PATH);
    }

    public void create(String name, Object value, String path) {
        create(name, value, DEFAULT_MAX_AGE, path);
    }

    public void create(String name, Object value, String path, int maxAge) {
        create(name, value, maxAge, path);
    }

    public void create(String name, Object value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, String.valueOf(value));
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        response.addCookie(cookie);
    }
}
