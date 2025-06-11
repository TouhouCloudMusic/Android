package net.hearnsoft.tcm.domain.model.song;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class SongSortingRule {
    public SongSortingStrategy strategy;
    public boolean reverse = false;

    public SongSortingRule(SongSortingStrategy strategy, boolean reverse) {
        this.strategy = strategy;
        this.reverse = reverse;
    }
}
