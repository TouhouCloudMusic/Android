package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.hearnsoft.tcm.databinding.FragmentLibraryBinding;
import net.hearnsoft.tcm.ui.activity.MainActivity;
import net.hearnsoft.tcm.ui.adapter.TestItemAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnScrollStateChangeListener;
import net.hearnsoft.tcm.ui.utils.BottomSpacingDecoration;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private List<TestItemAdapter.TestItemBean> testItemBeanList = new ArrayList<>();
    private OnScrollStateChangeListener scrollListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            scrollListener = ((MainActivity) context).getScrollListener();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        for (int i = 0; i < 30; i++) {
            String title = "Title " + i;
            TestItemAdapter.TestItemBean bean = new TestItemAdapter.TestItemBean();
            bean.setTitle(title);
            testItemBeanList.add(bean);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TestItemAdapter adapter = new TestItemAdapter(testItemBeanList);
        binding.testList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.testList.setLayoutManager(layoutManager);

        if (scrollListener != null) {
            binding.testList.addItemDecoration(new BottomSpacingDecoration(scrollListener));
        }

        binding.testList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (scrollListener == null) return;

                if (dy > 20) { // 向上滚动
                    scrollListener.hideBottomNav();
                } else if (dy < -5) { // 向下滚动
                    scrollListener.showBottomNav();
                }
            }
        });
    }
}
