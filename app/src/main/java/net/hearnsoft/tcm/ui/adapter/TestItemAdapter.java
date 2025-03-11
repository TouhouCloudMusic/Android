package net.hearnsoft.tcm.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.hearnsoft.tcm.databinding.ItemTestViewBinding;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class TestItemAdapter extends RecyclerView.Adapter<TestItemAdapter.TestItemViewHolder> {
    private ItemTestViewBinding binding;
    private List<TestItemBean> data;

    public TestItemAdapter(List<TestItemBean> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public TestItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ItemTestViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TestItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TestItemViewHolder holder, int position) {
        holder.bind(data, position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class TestItemViewHolder extends RecyclerView.ViewHolder {
        public TestItemViewHolder(ItemTestViewBinding binding) {
            super(binding.getRoot());
        }

        public void bind(List<TestItemBean> title, int position) {
            binding.itemTestTitle.setText(title.get(position).getTitle());
        }

    }

    @Getter
    @Setter
    public static class TestItemBean {
        private String title;
    }

}
