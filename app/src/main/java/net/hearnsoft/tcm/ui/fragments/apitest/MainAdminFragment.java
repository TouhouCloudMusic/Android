package net.hearnsoft.tcm.ui.fragments.apitest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.infrastructure.api.test.ApiTestConfig;
import net.hearnsoft.tcm.infrastructure.api.test.ApiTestEntry;
import net.hearnsoft.tcm.databinding.FragmentMainAdminBinding;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.adapter.ApiTestItemAdapter;
import net.hearnsoft.tcm.ui.utils.HorizontalSpaceItemDecoration;
import net.hearnsoft.tcm.utils.ApiConfigParser;
import net.hearnsoft.tcm.utils.Logs;

import java.io.IOException;
import java.io.InputStream;

public class MainAdminFragment extends Fragment {
    private FragmentMainAdminBinding binding;
    private ApiTestEntry apiTestEntry;
    private FragmentChangeListener listener;

    public void setFragmentChangeListener(FragmentChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.adminModeServerHost.setText("API Docs:" + Constants.API_DOCS);
        try (InputStream is = getResources().openRawResource(R.raw.api_test_config)) {
            ApiConfigParser parser = new ApiConfigParser();
            apiTestEntry = parser.parseApiConfigs(is);

            ApiTestItemAdapter adapter = new ApiTestItemAdapter();
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            );
            binding.adminTestModuleList.setLayoutManager(layoutManager);
            binding.adminTestModuleList.setAdapter(adapter);
            binding.adminTestModuleList.addItemDecoration(new HorizontalSpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.card_spacing)
            ));
            adapter.setOnItemClickListener(this::handleApiTestItemClicked);
            adapter.setData(apiTestEntry.getApiTestConfigs());
        } catch (IOException e) {
            Logs.e("MainAdminFragment", e.getMessage());
        }
    }

    private void handleApiTestItemClicked(ApiTestConfig config) {
        if (listener != null) {
            Fragment targetFragment = createFragmentByConfig(config);
            listener.onFragmentChange(targetFragment);
        }
    }

    private Fragment createFragmentByConfig(ApiTestConfig config) {
        // 根据config中的字符串创建对应的Fragment实例
        try {
            return (Fragment) Class.forName(config.getTestFragment()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Invalid fragment class", e);
        }
    }

    public interface FragmentChangeListener {
        void onFragmentChange(Fragment fragment);
    }

}
