package net.hearnsoft.tcm.domain.model.song;


import lombok.Value;

@Value
public class RecommendSong {
    int id;
    String title;
    String artist;
    String coverUrl;

    public RecommendSong(int id, String title, String artist, String coverUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverUrl = coverUrl;
    }

}
