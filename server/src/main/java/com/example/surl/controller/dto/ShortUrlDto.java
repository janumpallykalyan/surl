package com.example.surl.controller.dto;

import java.time.LocalDate;

public class ShortUrlDto {
    private String keyCode;
    private String  longUrl;
    private String userId;
    private LocalDate lastAccessDate;
    private Long totalVisits;

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(LocalDate lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public Long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(Long totalVisits) {
        this.totalVisits = totalVisits;
    }
}
