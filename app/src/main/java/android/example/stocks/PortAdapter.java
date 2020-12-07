package android.example.stocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PortAdapter extends RecyclerView.Adapter<PortAdapter.ViewHolder> implements ItemMoveCallbackPort.ItemTouchHelperContract {

    List<String> comptick, numshare, port_price, port_change;
    int list_size;
    Context context;

    public PortAdapter(Context context, List<String> comptick, List<String> numshare, List<String> port_price, List<String> port_change){
        this.comptick = comptick;
        this.numshare = numshare;
        this.port_price = port_price;
        this.port_change = port_change;
        list_size = comptick.size();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.one_stock, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fav_tick.setText(comptick.get(position));
        holder.fav_detail.setText(numshare.get(position));

        holder.fav_change.setText(port_change.get(position));
        holder.fav_lp.setText(port_price.get(position));

        if(Double.parseDouble(port_change.get(position)) < 0){
            holder.fav_arrow.setBackgroundResource(R.drawable.ic_baseline_trending_down_24);
            holder.fav_change.setTextColor(ContextCompat.getColor(holder.fav_change.getContext(), R.color.red));
        } else {
            holder.fav_arrow.setBackgroundResource(R.drawable.ic_twotone_trending_up_24);
            holder.fav_change.setTextColor(ContextCompat.getColor(holder.fav_change.getContext(), R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return list_size;
    }

    public void update_lists(List<String> comptick, List<String> numshare, List<String> port_price, List<String> port_change){
        this.comptick = comptick;
        this.numshare = numshare;
        this.port_price = port_price;
        this.port_change = port_change;
        list_size = comptick.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(comptick, i, i + 1);
                Collections.swap(numshare, i, i + 1);
                Collections.swap(port_price, i, i + 1);
                Collections.swap(port_change, i, i + 1);
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("shared_prefs", MODE_PRIVATE);
            String port_order = sharedPreferences.getString("port_order_list", "");

            SharedPreferences.Editor editor = sharedPreferences.edit();

            port_order = "";

            for(int i = 0; i<comptick.size(); ++i){
                port_order = port_order + comptick.get(i) + "_";
            }

            editor.putString("port_order_list", port_order);
            editor.commit();

        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(comptick, i, i - 1);
                Collections.swap(numshare, i, i - 1);
                Collections.swap(port_price, i, i - 1);
                Collections.swap(port_change, i, i - 1);
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            String port_order = sharedPreferences.getString("port_order_list", "");

            SharedPreferences.Editor editor = sharedPreferences.edit();

            port_order = "";

            for(int i = 0; i<comptick.size(); ++i){
                port_order = port_order + comptick.get(i) + "_";
            }

            editor.putString("port_order_list", port_order);
            editor.commit();
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(myViewHolder.itemView.getContext(), R.color.grey));
    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(myViewHolder.itemView.getContext(), R.color.white));
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView fav_tick, fav_detail, fav_lp, fav_change;
        ImageView fav_arrow, fav_goto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fav_tick = itemView.findViewById(R.id.fav_tick);
            fav_detail = itemView.findViewById(R.id.fav_detail);
            fav_change = itemView.findViewById(R.id.fav_change);
            fav_lp = itemView.findViewById(R.id.fav_lp);

            fav_arrow = itemView.findViewById(R.id.fav_arrow);
            fav_goto = itemView.findViewById(R.id.fav_goto);

            fav_goto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), DetailsActivity.class);
                    intent.putExtra("QUERY", comptick.get(getAdapterPosition()));
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
