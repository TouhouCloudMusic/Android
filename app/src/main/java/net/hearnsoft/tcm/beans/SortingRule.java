package net.hearnsoft.tcm.beans;

import net.hearnsoft.tcm.enums.SortingStrategy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SortingRule {
    private SortingStrategy strategy;
    private boolean reverse = false;

    public SortingRule(SortingStrategy strategy, boolean reverse) {
        this.strategy = strategy;
        this.reverse = reverse;
    }
}
