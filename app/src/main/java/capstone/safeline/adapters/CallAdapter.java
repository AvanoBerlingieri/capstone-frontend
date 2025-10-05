package capstone.safeline.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import capstone.safeline.R;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder> {

    private final List<String> callList;

    public CallAdapter(List<String> callList) {
        this.callList = callList;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        String call = callList.get(position);
        holder.tvCall.setText(call);
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView tvCall;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCall = itemView.findViewById(R.id.tvCallItem);
        }
    }
}
