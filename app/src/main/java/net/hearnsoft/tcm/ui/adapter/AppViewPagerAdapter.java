package net.hearnsoft.tcm.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<Long> ids = new ArrayList<>();
    private static long nextId = 0;

    public AppViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        this(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle());
    }

    public AppViewPagerAdapter(@NonNull Fragment fragment) {
        this(fragment.getChildFragmentManager(), fragment.getLifecycle());
    }

    public AppViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    /**
     * 根据上下文选择合适的FragmentManager创建适配器
     * @param context 可以是Fragment或FragmentActivity
     * @return 适合当前层级的ViewPagerAdapter实例
     */
    public static AppViewPagerAdapter create(Object context) {
        if (context instanceof Fragment) {
            return new AppViewPagerAdapter((Fragment) context);
        } else if (context instanceof FragmentActivity) {
            return new AppViewPagerAdapter((FragmentActivity) context);
        } else {
            throw new IllegalArgumentException("Context must be Fragment or FragmentActivity");
        }
    }

    public void addFragment(Fragment fragment) {
        if (fragments != null && !fragments.contains(fragment)) {
            fragments.add(fragment);
            ids.add(nextId++);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return fragments.get(position);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @Override
    public long getItemId(int position) {
        return ids.get(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        return ids.contains(itemId);
    }

    public void removeFragment(int position) {
        if (fragments != null && position < fragments.size()) {
            fragments.remove(position);
            ids.remove(position);
        }
    }

    public void clearFragments() {
        fragments.clear();
        ids.clear();
    }
}
