package net.hearnsoft.tcm.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T,VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>  {

    protected List<T> dataList = new ArrayList<>();

    public void setData(List<T> data) {
        this.dataList.clear();
        if (data != null) {
            this.dataList.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        if (data != null) {
            int startPosition = this.dataList.size();
            this.dataList.addAll(data);
            notifyItemRangeInserted(startPosition, data.size());
        }
    }

    public List<T> getData() {
        if (dataList != null) {
            return dataList;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return dataList.isEmpty() ? 0 : dataList.size();
    }
}
