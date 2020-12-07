package android.example.stocks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder> implements FavAdapterInterface {

    List<String> comp_names, fav_tickers, fav_changes, fav_prices;
    int list_len;

    public FavAdapter(List<String> comp_names, List<String> fav_tickers, List<String> fav_prices, List<String> fav_changes) {
        this.comp_names = comp_names;
        this.fav_changes = fav_changes;
        this.fav_prices = fav_prices;
        this.fav_tickers = fav_tickers;
        list_len = fav_tickers.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.one_stock, parent, false);
        FavAdapter.ViewHolder viewHolder = new FavAdapter.ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fav_tick.setText(fav_tickers.get(position));
        holder.fav_detail.setText(comp_names.get(position));
        holder.fav_change.setText(fav_changes.get(position));
        holder.fav_lp.setText(fav_prices.get(position));

        if(Double.parseDouble(fav_changes.get(position)) < 0){
            holder.fav_arrow.setBackgroundResource(R.drawable.ic_baseline_trending_down_24);
            holder.fav_change.setTextColor(ContextCompat.getColor(holder.fav_change.getContext(), R.color.red));
        } else {
            holder.fav_arrow.setBackgroundResource(R.drawable.ic_twotone_trending_up_24);
            holder.fav_change.setTextColor(ContextCompat.getColor(holder.fav_change.getContext(), R.color.green));
        }
    }


    @Override
    public int getItemCount() {
        return list_len;
    }

    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(myViewHolder.itemView.getContext(),R.color.grey));

    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(myViewHolder.itemView.getContext(),R.color.white));

    }


    public void update_data(List<String> comp_names, List<String> fav_tickers, List<String> fav_prices, List<String> fav_changes){
        this.comp_names = comp_names;
        this.fav_changes = fav_changes;
        this.fav_prices = fav_prices;
        this.fav_tickers = fav_tickers;
        list_len = fav_tickers.size();
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
                    intent.putExtra("QUERY", fav_tickers.get(getAdapterPosition()));
                    itemView.getContext().startActivity(intent);
                }
            });

        }


    }
}
