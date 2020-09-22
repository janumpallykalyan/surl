package com.example.surl.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Document(collection = "shorturl_log")
public class ShortUrlLog implements Serializable {

    @Id
    private String id;

    @Indexed
    private Long keyCode;

    private String accessedByUser;

    private String accessedByBrowser;

    private String accessedByDevice;

    private LocalDate accessedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(Long keyCode) {
        this.keyCode = keyCode;
    }

    public String getAccessedByUser() {
        return accessedByUser;
    }

    public void setAccessedByUser(String accessedByUser) {
        this.accessedByUser = accessedByUser;
    }

    public String getAccessedByBrowser() {
        return accessedByBrowser;
    }

    public void setAccessedByBrowser(String accessedByBrowser) {
        this.accessedByBrowser = accessedByBrowser;
    }

    public String getAccessedByDevice() {
        return accessedByDevice;
    }

    public void setAccessedByDevice(String accessedByDevice) {
        this.accessedByDevice = accessedByDevice;
    }

    public LocalDate getAccessedDate() {
        return accessedDate;
    }

    public void setAccessedDate(LocalDate accessedDate) {
        this.accessedDate = accessedDate;
    }
}
