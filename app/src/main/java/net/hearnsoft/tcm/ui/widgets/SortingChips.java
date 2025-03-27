package net.hearnsoft.tcm.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuItem;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

public class SortingChips extends Chip {
    private SortedMap<SongSortingStrategy, Integer> sortingStrategies = new TreeMap<>();
    private SongSortingRule sortingRule;
    private Consumer<SongSortingRule> onSortingRuleSelected;
    private PopupMenu popupMenu;

    public SortingChips(Context context) {
        super(context);
        init(context, null);
    }

    public SortingChips(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SortingChips(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setChipBackgroundColor(MaterialColors.getColorStateListOrNull(
            context,
            com.google.android.material.R.attr.colorPrimaryContainer
        ));
        setChipStrokeWidth(0f);
        setText(R.string.sort_by_unknown);
        setCloseIcon(R.drawable.ic_arrow_drop_down);

        setOnClickListener(v -> openPopupMenu());

        popupMenu = new PopupMenu(context, this);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            SongSortingStrategy strategy = SongSortingStrategy.values()[item.getItemId()];
            boolean reverse = sortingRule != null && sortingRule.getStrategy() == strategy
                ? !sortingRule.isReverse()
                : sortingRule != null && sortingRule.isReverse();

            if (onSortingRuleSelected != null) {
                onSortingRuleSelected.accept(new SongSortingRule(strategy, reverse));
            }
            return true;
        });
    }

    public void setSortingStrategies(@NonNull SortedMap<SongSortingStrategy, Integer> sortingStrategies) {
        this.sortingStrategies = sortingStrategies;

        popupMenu.getMenu().clear();
        for (SongSortingStrategy sortingStrategy : sortingStrategies.keySet()) {
            int stringResId = sortingStrategies.get(sortingStrategy);
            MenuItem item = popupMenu.getMenu().add(0, sortingStrategy.ordinal(), 0, stringResId);
            item.setIconTintList(MaterialColors.getColorStateListOrNull(
                getContext(),
                com.google.android.material.R.attr.colorOnSurface
            ));
        }
    }

    public void setOnSortingRuleSelectedListener(Consumer<SongSortingRule> listener) {
        this.onSortingRuleSelected = listener;
    }

    public void setSortingRule(SongSortingRule sortingRule) {
        this.sortingRule = sortingRule;

        Integer textResId = sortingStrategies.get(sortingRule.getStrategy());
        setText(textResId != null ? textResId : R.string.sort_by_unknown);

        setChipIcon(sortingRule.isReverse()
            ? R.drawable.ic_sort_alphabetical_descending
            : R.drawable.ic_sort_alphabetical_ascending);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            boolean isCurrentStrategy = item.getItemId() == sortingRule.getStrategy().ordinal();

            if (isCurrentStrategy) {
                item.setIcon(sortingRule.isReverse()
                    ? R.drawable.ic_sort_alphabetical_descending
                    : R.drawable.ic_sort_alphabetical_ascending);
            } else {
                item.setIcon(null);
            }
        }
    }

    private void setChipIcon(@DrawableRes int icon) {
        setChipIconResource(icon);
        setChipIconVisible(true);
        setChipIconTint(MaterialColors.getColorStateListOrNull(
            getContext(),
            com.google.android.material.R.attr.colorOnSurface
        ));
    }

    private void setCloseIcon(@DrawableRes int icon) {
        setCloseIconResource(icon);
        setCloseIconVisible(true);
        setCloseIconTint(MaterialColors.getColorStateListOrNull(
            getContext(),
            com.google.android.material.R.attr.colorOnSurface
        ));
    }

    private void openPopupMenu() {
        int size = sortingStrategies.size();
        if (size == 0) {
            return;
        } else if (size == 1) {
            SongSortingStrategy strategy = sortingStrategies.keySet().iterator().next();
            boolean reverse = sortingRule != null && sortingRule.getStrategy() == strategy
                ? !sortingRule.isReverse()
                : sortingRule != null && sortingRule.isReverse();

            if (onSortingRuleSelected != null) {
                onSortingRuleSelected.accept(new SongSortingRule(strategy, reverse));
            }
        } else {
            popupMenu.show();
        }
    }
}
