package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class HistoryItem {
    private String uri;
    private String title;
    private LocalDateTime createdAt;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
        return "HistoryItem : " +
                "uri=" + uri + ' ' +
                " title='" + title + ' ' +
                " createdAt=" + dtf.format(createdAt);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryItem that = (HistoryItem) o;
        return uri.equals(that.uri) &&
                title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, title, createdAt);
    }
}
