package net.hearnsoft.tcm.domain.model.song;

import lombok.Data;

@Data
public class SongSortingRule {
    private SongSortingStrategy strategy;
    private boolean reverse = false;

    public SongSortingRule(SongSortingStrategy strategy, boolean reverse) {
        this.strategy = strategy;
        this.reverse = reverse;
    }
}
