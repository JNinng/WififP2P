package top.ninng.demo.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.ninng.demo.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

    public OnItemClickListener mOnItemClickListener;

    /*
     * 设置回调接口，实现点击与长按
     * */
    private List<Map<String, String>> mList;

    public MyAdapter() {
        super();
        mList = new ArrayList<>();
    }

    public MyAdapter(List<Map<String, String>> list) {
        super();
        this.mList = list;
    }

    public interface OnItemClickListener {
        void OnItemClick(View view, int position);

        void OnItemLongClick(View view, int position);
    }

    class MyHolder extends RecyclerView.ViewHolder {

        public TextView tvname;
        public TextView tvaddress;

        public MyHolder(View View) {
            super(View);
            tvname = (TextView) View.findViewById(R.id.tv_name);
            tvaddress = (TextView) View.findViewById(R.id.tv_address);

        }
    }

    public void SetOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public Map<String, String> getData(int position) {
        return mList.get(position);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.card_item, parent, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.tvname.setText(mList.get(position).get("name"));
        holder.tvaddress.setText(mList.get(position).get("address"));

        if (mOnItemClickListener != null) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnItemClick(holder.itemView, position);
                }

            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.OnItemLongClick(holder.itemView, position);
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setList(List<Map<String, String>> mList) {
        this.mList = mList;
        notifyDataSetChanged();
//        notifyItemInserted(mList.size());
    }
}