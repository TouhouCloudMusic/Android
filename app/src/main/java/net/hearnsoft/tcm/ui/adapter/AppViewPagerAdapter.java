package net.hearnsoft.tcm.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppViewPagerAdapter extends FragmentStateAdapter {

    private List<Class> fragments;

    public AppViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
    }

    public void addFragment(Fragment fragment) {
        if (fragments != null) {
            fragments.add(fragment.getClass());
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return (Fragment) fragments.get(position).newInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

}
