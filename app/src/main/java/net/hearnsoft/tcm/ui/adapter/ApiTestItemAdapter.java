package net.hearnsoft.tcm.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.hearnsoft.tcm.infrastructure.api.test.ApiTestConfig;
import net.hearnsoft.tcm.databinding.ItemApiTestBinding;
import net.hearnsoft.tcm.ui.fragments.apitest.ApiTestInterface;

public class ApiTestItemAdapter
        extends BaseAdapter<ApiTestConfig, ApiTestItemAdapter.ApiTestItemViewHolder> {

    private ApiTestInterface listener;

    public void setOnItemClickListener(ApiTestInterface listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApiTestItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApiTestBinding binding = ItemApiTestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ApiTestItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ApiTestItemViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    class ApiTestItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemApiTestBinding binding;

        public ApiTestItemViewHolder(ItemApiTestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ApiTestConfig config) {
            binding.apiNameTitle.setText(config.getApiName());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.handleApiTestItemClicked(config);
                }
            });
        }
    }
}
