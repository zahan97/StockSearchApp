package android.example.stocks;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    List<String> titles; List<String> sources; List<String> images; List<String> t_stamps; List<String> news_urls;

    public NewsAdapter(List<String> titles, List<String> sources, List<String> images, List<String> t_stamps, List<String> news_urls){
        this.titles = titles;
        this.sources = sources;
        this.images = images;
        this.t_stamps = t_stamps;
        this.news_urls = news_urls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.news_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.news_src.setText(sources.get(position+1));
            holder.news_head.setText(titles.get(position+1));
            holder.news_time.setText(t_stamps.get(position+1));
            Picasso.with(holder.news_img.getContext()).load(images.get(position+1)).error(R.drawable.no_image).fit().centerCrop().into(holder.news_img);
    }

    @Override
    public int getItemCount() {
        return titles.size()-1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView news_src, news_time, news_head;
        ImageView news_img;
        Dialog dialog;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            news_img = itemView.findViewById(R.id.news_img);
            news_src = itemView.findViewById(R.id.news_src);
            news_time = itemView.findViewById(R.id.news_time);
            news_head = itemView.findViewById(R.id.news_head);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String temp_url = news_urls.get(getAdapterPosition()+1);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url));
            v.getContext().startActivity(browserIntent);
        }

        @Override
        public boolean onLongClick(View v) {
            dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.news_dialog);

            ImageView dialog_img = dialog.findViewById(R.id.dialog_image);
            Picasso.with(dialog_img.getContext()).load(images.get(getAdapterPosition()+1)).error(R.drawable.no_image_big).fit().centerCrop().into(dialog_img);

            TextView dialog_text = dialog.findViewById(R.id.dialog_title);
            dialog_text.setText(titles.get(getAdapterPosition()+1));

            dialog.show();

            ImageView dialog_twitter = dialog.findViewById(R.id.dialog_twitter);
            ImageView dialog_chrome = dialog.findViewById(R.id.dialog_chrome);

            dialog_chrome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp_url = news_urls.get(getAdapterPosition()+1);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url));
                    v.getContext().startActivity(browserIntent);
                }
            });

            dialog_twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp_url = news_urls.get(getAdapterPosition()+1);
                    String temp_url_main = "https://twitter.com/intent/tweet?text=" + "Check out this Link: "+ temp_url + "&hashtags=CSCI571StockApp";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url_main));
                    v.getContext().startActivity(browserIntent);
                }
            });


            return true;
        }
    }
}
