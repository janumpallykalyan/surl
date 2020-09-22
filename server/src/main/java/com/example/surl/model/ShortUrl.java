package com.example.surl.model;

import com.example.surl.model.embedded.Stats;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "shorturl")
public class ShortUrl implements Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    private String id;
    @Indexed
    private Long keyCode;
    private LocalDate createdDate;
    private LocalDate lastAccessDate;
    private LocalDate expiryDate;
    @Indexed
    private String longUrl;
    @Indexed
    private String userId;

    private boolean notifiedExpiry;

    private Stats stats;


    public ShortUrl() {
    }

    public ShortUrl(Long keyCode) {
        this.keyCode = keyCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Long getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(Long keyCode) {
        this.keyCode = keyCode;
    }

    public LocalDate getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(LocalDate lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isNotifiedExpiry() {
        return notifiedExpiry;
    }

    public void setNotifiedExpiry(boolean notifiedExpiry) {
        this.notifiedExpiry = notifiedExpiry;
    }
}
