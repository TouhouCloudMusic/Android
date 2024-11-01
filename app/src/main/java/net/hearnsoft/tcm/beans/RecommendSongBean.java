package net.hearnsoft.tcm.beans;

public class RecommendSongBean {
    private int id;
    private String title;
    private String artist;
    private String coverUrl;

    public RecommendSongBean(int id, String title, String artist, String coverUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverUrl = coverUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
