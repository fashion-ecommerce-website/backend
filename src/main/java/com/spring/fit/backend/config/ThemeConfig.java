package com.spring.fit.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.theme")
public class ThemeConfig {

    private String defaultTheme = "light";
    private List<String> availableThemes = List.of("light", "dark", "auto");
    private boolean persistent = true;
    private String cookieName = "user-theme";
    private String cookiePath = "/";
    private int cookieMaxAge = 31536000; // 1 year in seconds

    // Getters and Setters
    public String getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public List<String> getAvailableThemes() {
        return availableThemes;
    }

    public void setAvailableThemes(List<String> availableThemes) {
        this.availableThemes = availableThemes;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public int getCookieMaxAge() {
        return cookieMaxAge;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }
}