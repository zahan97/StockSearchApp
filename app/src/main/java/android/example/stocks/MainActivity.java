package android.example.stocks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;

    private AutoSuggestAdapter autoSuggestAdapter;
    private static final int TRIGGER_AUTO_COMPLETE = 300;
    private static final long AUTO_COMPLETE_DELAY = 400;
    private Handler handler;
    Boolean oncrt;

    RecyclerView portview, favview;
    PortAdapter padp;
    FavAdapter fadp;

    RequestQueue queue;
    String url = "https://enduring-victor-294122.wn.r.appspot.com/autocomplete?symbol=";
    JsonArrayRequest jsonArrayRequest;

    NestedScrollView nestedScrollView;
    LinearLayout linearLayout;

    Boolean fav_created = false;

    String url_lp = "https://enduring-victor-294122.wn.r.appspot.com/latsp?symbol=";
    //String url_desc = "https://enduring-victor-294122.wn.r.appspot.com/compdesc?symbol=";

    List<String> comptick;
    List<String> numshare;
    List<String> port_prices;
    List<String> port_changes;

    List<String> comp_names;
    List<String> fav_tickers;
    List<String> fav_prices;
    List<String> fav_changes;
    public static final String tick_set = "tick_set";
    public static final String port_set = "port_set";
    public static final String fav_set = "fav_set";
    public static final String ORDER_FAV = "ORDER_FAV";
    public static final String tick_set_names = "tick_set_names";
    public static final String tick_set_b = "tick_set_b";
    public static final String cashflow = "cashflow";
    public static final String num_of_shares = "num_of_shares";
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String port_order_list = "port_order_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nestedScrollView = findViewById(R.id.main_at);
        linearLayout = findViewById(R.id.progress_layout);

        nestedScrollView.setVisibility(View.INVISIBLE);
        //nestedScrollView.setVisibility(View.VISIBLE);

        Handler m = new Handler();
        m.postDelayed(() -> {
            linearLayout.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);
        },3000);


        new Handler().postDelayed(() -> update_them(), 15000);

        queue = Volley.newRequestQueue(this);

        oncrt= true;

        make_portfolio();

        make_favorites();

        TextView tiingo = findViewById(R.id.tiingo);
        tiingo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/")));
            }
        });

    }

    public void update_them(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> port_shares = new HashSet<>(sharedPreferences.getStringSet(port_set, new HashSet<>()));
        Set<String> favs = new HashSet<>(sharedPreferences.getStringSet(fav_set, new HashSet<>()));
        Log.v("PRICE_UPDATE", "Updated Triggered");

        if(port_shares.size() > 0){
            change_portfolio();
        }

        if(favs.size() > 0){
            change_fav();
        }

        new Handler().postDelayed(() -> update_them(), 15000);
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void make_portfolio(){
        portview = findViewById(R.id.portview);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> port_shares = new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<String>()));
        String port_order = sharedPreferences.getString(port_order_list, "");
        String numshares = sharedPreferences.getString(num_of_shares,"");
        SharedPreferences.Editor edit_port = sharedPreferences.edit();
        String port_tick_lis = "";

        String money = sharedPreferences.getString(cashflow, "20000.00");
        TextView money_text = findViewById(R.id.net_worth);
        //money_text.setText(money);

        comptick = new ArrayList<>();
        numshare = new ArrayList<>();
        port_prices = new ArrayList<>();
        port_changes = new ArrayList<>();

        if(port_shares.size() > 0){
            portview.setVisibility(View.VISIBLE);
            String[] num_shares = numshares.split("_");

            HashMap<String,String> name_to_share = new HashMap<>();
            HashMap<String, Double> name_to_amt_share = new HashMap<>();

            for(int i = 0; i<num_shares.length; ++i){
                String[] zs = num_shares[i].split("\\$");
                //comptick.add(zs[0]);
                port_tick_lis = port_tick_lis + zs[0] + ",";
                Double temp_num_share = Double.parseDouble(zs[1]);
                //numshare.add(String.format("%.1f", temp_num_share) + " shares");
                name_to_share.put(zs[0], String.format("%.2f", temp_num_share) + " shares");
                name_to_amt_share.put(zs[0], temp_num_share);
            }

            String[] porder = port_order.split("_");
            for(String i : porder){
                comptick.add(i);
                numshare.add(name_to_share.get(i));
            }

            JsonArrayRequest req_prices = new JsonArrayRequest(Request.Method.GET, url_lp + port_tick_lis, null, response -> {
                HashMap<String,String> temp_map_lp = new HashMap<>();
                HashMap<String,String> temp_map_chg = new HashMap<>();

                for(int i = 0; i<response.length(); ++i){

                    try {
                        JSONObject temp = response.getJSONObject(i);
                        double share_price = Double.parseDouble(temp.getString("last"));
                        temp_map_lp.put(temp.getString("ticker"), String.format("%.2f", share_price));
                        Double change = temp.getDouble("last") - temp.getDouble("prevClose");
                        temp_map_chg.put(temp.getString("ticker"), String.format("%.2f", change));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                double sum_total = 0;
                for(int i = 0; i<comptick.size(); ++i){
                    sum_total = sum_total + (Double.parseDouble(Objects.requireNonNull(temp_map_lp.get(comptick.get(i)))) * name_to_amt_share.get(comptick.get(i)));
                    port_prices.add(temp_map_lp.get(comptick.get(i)));
                    port_changes.add(temp_map_chg.get(comptick.get(i)));
                }

                sum_total = sum_total + Double.parseDouble(money);
                money_text.setText(String.format("%.2f", sum_total));

                padp = new PortAdapter(MainActivity.this, comptick, numshare, port_prices, port_changes);
                portview.setAdapter(padp);
                ViewCompat.setNestedScrollingEnabled(portview,false);

                ItemTouchHelper.Callback callback = new ItemMoveCallbackPort(padp);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(portview);

                RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(portview.getContext(),R.drawable.divider));
                portview.addItemDecoration(dividerItemDecoration);

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(req_prices);
        } else {
            portview.setVisibility(View.GONE);
            money_text.setText("20000.00");
        }

    }

    @SuppressLint("SetTextI18n")
    public void change_portfolio(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> port_shares = new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<String>()));
        String port_order = sharedPreferences.getString(port_order_list, "");
        String numshares = sharedPreferences.getString(num_of_shares,"");
        SharedPreferences.Editor edit_port = sharedPreferences.edit();
        String port_tick_lis = "";

        HashMap<String,String> name_to_share = new HashMap<>();
        HashMap<String, Double> name_to_amt_share = new HashMap<>();

        TextView money_text = findViewById(R.id.net_worth);

        if(port_shares.size() > 0){
            portview.setVisibility(View.VISIBLE);
            String[] num_shares = numshares.split("_");

            comptick.clear();
            numshare.clear();
            port_prices.clear();
            port_changes.clear();

            for(int i = 0; i<num_shares.length; ++i){
                String[] zs = num_shares[i].split("\\$");
                //comptick.add(zs[0]);
                port_tick_lis = port_tick_lis + zs[0] + ",";
                Double temp_num_share = Double.parseDouble(zs[1]);
                //numshare.add(String.format("%.1f", temp_num_share) + " shares");
                name_to_share.put(zs[0], String.format("%.2f", temp_num_share) + " shares");
                name_to_amt_share.put(zs[0], temp_num_share);
            }

            String[] porder = port_order.split("_");
            for(String i : porder){
                comptick.add(i);
                numshare.add(name_to_share.get(i));
            }

            JsonArrayRequest req_prices = new JsonArrayRequest(Request.Method.GET, url_lp + port_tick_lis, null, new Response.Listener<JSONArray>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onResponse(JSONArray response) {
                    HashMap<String,String> temp_map_lp = new HashMap<>();
                    HashMap<String,String> temp_map_chg = new HashMap<>();

                    String money = sharedPreferences.getString(cashflow, "20000.00");

                    for(int i = 0; i<response.length(); ++i){

                        try {
                            JSONObject temp = response.getJSONObject(i);
                            double share_price = Double.parseDouble(temp.getString("last"));
                            temp_map_lp.put(temp.getString("ticker"), String.format("%.2f", share_price));
                            Double change = temp.getDouble("last") - temp.getDouble("prevClose");
                            temp_map_chg.put(temp.getString("ticker"), String.format("%.2f", change));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    double sum_total = 0;
                    for(int i = 0; i<comptick.size(); ++i){
                        sum_total = sum_total + (Double.parseDouble(Objects.requireNonNull(temp_map_lp.get(comptick.get(i)))) * name_to_amt_share.get(comptick.get(i)));
                        port_prices.add(temp_map_lp.get(comptick.get(i)));
                        port_changes.add(temp_map_chg.get(comptick.get(i)));
                    }

                    sum_total = sum_total + Double.parseDouble(money);
                    money_text.setText(String.format("%.2f", sum_total));

                    padp.update_lists(comptick, numshare, port_prices, port_changes);
                    padp.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(req_prices);
        } else {
            portview.setVisibility(View.GONE);
            money_text.setText("20000.00");
        }
    }

    public void make_favorites(){
        favview = findViewById(R.id.favview);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> favs = new HashSet<>(sharedPreferences.getStringSet(tick_set, new HashSet<String>()));
        Set<String> fav_names = new HashSet<>(sharedPreferences.getStringSet(tick_set_names, new HashSet<String>()));
        Set<String> port_shares = new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<String>()));
        String numshares = sharedPreferences.getString(num_of_shares,"");
        SharedPreferences.Editor edit_order_fav = sharedPreferences.edit();

        HashMap<String,String> port_map = new HashMap<>();
        String[] num_shares = numshares.split("_");

        if(port_shares.size() > 0){
            for(int i = 0; i<num_shares.length; ++i){
                String[] zs = num_shares[i].split("\\$");
                port_map.put(zs[0], zs[1]);
            }
        }

        fav_created = true;
        comp_names = new ArrayList<>();
        fav_tickers = new ArrayList<>();
        fav_changes = new ArrayList<>();
        fav_prices = new ArrayList<>();

        if(favs.size() > 0){
            favview.setVisibility(View.VISIBLE);
            String fav_tick_lis = "";

            for(String t : favs){
                fav_tickers.add(t);
                fav_tick_lis = fav_tick_lis + t + ",";
            }

            String order_fav = sharedPreferences.getString(ORDER_FAV, "");
            if(order_fav.equals("") && favs.size() == 1){
                for(String i:favs)
                    order_fav = order_fav + i + "_";

                edit_order_fav.putString(ORDER_FAV, order_fav);
                edit_order_fav.commit();
            } else {
                String[] order_fav_array = order_fav.split("_");
                if(order_fav_array.length == favs.size()){
                    fav_tickers.clear();
                    for(int i = 0; i<order_fav_array.length; i++){
                        fav_tickers.add(order_fav_array[i]);
                    }
                }
                else {
                    for(String j:favs){
                        List<String> templ = new ArrayList<String>(Arrays.asList(order_fav_array));
                        if(!templ.contains(j)){
                            order_fav = order_fav + j + "_";
                            edit_order_fav.putString(ORDER_FAV, order_fav);
                            edit_order_fav.commit();
                            break;
                        }
                    }

                    String[] new_order_fav_array = order_fav.split("_");
                    fav_tickers.clear();
                    for(int i = 0; i<new_order_fav_array.length; i++){
                        fav_tickers.add(new_order_fav_array[i]);
                    }

                }
            }

            HashMap<String,String> temp_map_name = new HashMap<>();
            for(String t:fav_names){
                String[] tp = t.split("_");
                temp_map_name.put(tp[0], tp[1]);
            }

            for(int i = 0; i<fav_tickers.size(); ++i){
                if(port_shares.contains(fav_tickers.get(i))){
                    Double t = Double.parseDouble(port_map.get(fav_tickers.get(i)));
                    comp_names.add(String.format("%.2f", t) + " shares");
                }
                else
                    comp_names.add(temp_map_name.get(fav_tickers.get(i)));
            }


            JsonArrayRequest req_prices = new JsonArrayRequest(Request.Method.GET, url_lp + fav_tick_lis, null, new Response.Listener<JSONArray>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onResponse(JSONArray response) {
                    HashMap<String,String> temp_map_lp = new HashMap<>();
                    HashMap<String,String> temp_map_chg = new HashMap<>();

                    for(int i = 0; i<response.length(); ++i){
                        try {
                            JSONObject temp = response.getJSONObject(i);
                            double share_price = Double.parseDouble(temp.getString("last"));
                            temp_map_lp.put(temp.getString("ticker"), String.format("%.2f", share_price));

                            Double change = temp.getDouble("last") - temp.getDouble("prevClose");
                            temp_map_chg.put(temp.getString("ticker"), String.format("%.2f", change));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    for(int i = 0; i<fav_tickers.size(); ++i){
                        fav_prices.add(temp_map_lp.get(fav_tickers.get(i)));
                        fav_changes.add(temp_map_chg.get(fav_tickers.get(i)));
                    }



                    fadp = new FavAdapter(comp_names, fav_tickers, fav_prices, fav_changes);
                    favview.setAdapter(fadp);

                    ViewCompat.setNestedScrollingEnabled(favview,false);
                    RecyclerView.ItemDecoration dividerItemDecorationf = new DividerItemDecorator(ContextCompat.getDrawable(favview.getContext(),R.drawable.divider));
                    favview.addItemDecoration(dividerItemDecorationf);

                    ItemTouchHelper.Callback simpleCallback_fav = new ItemTouchHelper.Callback() {
                        @Override
                        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                            int swipe = ItemTouchHelper.LEFT;
                            return makeMovementFlags(dragFlags, swipe);
                        }

                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            int from_position = viewHolder.getAdapterPosition();
                            int to_position = target.getAdapterPosition();
                            String fresh_order = "";

                            if(from_position < to_position){
                                for (int i = from_position; i < to_position; i++) {
                                    Collections.swap(fav_tickers, i, i+1);
                                    Collections.swap(fav_changes, i, i+1);
                                    Collections.swap(fav_prices, i, i+1);
                                    Collections.swap(comp_names, i, i+1);
                                }

                                for(int i = 0; i<fav_tickers.size(); ++i){
                                    fresh_order = fresh_order + fav_tickers.get(i)+"_";
                                }

                                edit_order_fav.putString(ORDER_FAV, fresh_order);
                                edit_order_fav.commit();

                            } else {
                                for (int i = from_position; i > to_position; i--) {
                                    Collections.swap(fav_tickers, i, i-1);
                                    Collections.swap(fav_changes, i, i-1);
                                    Collections.swap(fav_prices, i, i-1);
                                    Collections.swap(comp_names, i, i-1);
                                }

                                for(int i = 0; i<fav_tickers.size(); ++i){
                                    fresh_order = fresh_order + fav_tickers.get(i)+"_";
                                }

                                edit_order_fav.putString(ORDER_FAV, fresh_order);
                                edit_order_fav.commit();
                            }

                            fadp.notifyItemMoved(from_position, to_position);

                            return false;
                        }

                        @Override
                        public boolean isLongPressDragEnabled() {
                            return true;
                        }

                        @Override
                        public boolean isItemViewSwipeEnabled() { return true; }

                        @Override
                        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                            if(viewHolder instanceof FavAdapter.ViewHolder){
                                FavAdapter.ViewHolder myVH = (FavAdapter.ViewHolder) viewHolder;
                                fadp.onRowSelected(myVH);
                            }

                            super.onSelectedChanged(viewHolder, actionState);
                        }

                        @Override
                        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                            super.clearView(recyclerView, viewHolder);

                            if(viewHolder instanceof FavAdapter.ViewHolder){
                                FavAdapter.ViewHolder myVH = (FavAdapter.ViewHolder) viewHolder;
                                fadp.onRowClear(myVH);
                            }
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            Set<String> favs_new = new HashSet<>(sharedPreferences.getStringSet(tick_set, new HashSet<String>()));
                            Set<String> fav_names_new = new HashSet<>(sharedPreferences.getStringSet(tick_set_names, new HashSet<String>()));
                            SharedPreferences.Editor editor_fav = sharedPreferences.edit();
                            favs_new.remove(fav_tickers.get(position));
                            fav_names_new.remove(fav_tickers.get(position)+"_"+comp_names.get(position));

                            editor_fav.putStringSet(tick_set, favs_new);
                            editor_fav.putStringSet(tick_set_names, fav_names_new);

                            fav_tickers.remove(position);

                            String new_order = "";
                            for(int i = 0; i<fav_tickers.size(); ++i){
                                new_order = new_order + fav_tickers.get(i) + "_";
                            }

                            editor_fav.putString(ORDER_FAV, new_order);
                            editor_fav.commit();

                            comp_names.remove(position);
                            fav_prices.remove(position);
                            fav_changes.remove(position);

                            fadp.update_data(comp_names, fav_tickers, fav_prices, fav_changes);
                            fadp.notifyItemRemoved(viewHolder.getAdapterPosition());
                        }


                        @Override
                        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.swipe_red))
                                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                                    .create()
                                    .decorate();

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    };

                    ItemTouchHelper itemTouchHelper_fav = new ItemTouchHelper(simpleCallback_fav);
                    itemTouchHelper_fav.attachToRecyclerView(favview);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(req_prices);
        } else{
            favview.setVisibility(View.INVISIBLE);
        }
    }

    public void change_fav(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> favs = new HashSet<>(sharedPreferences.getStringSet(tick_set, new HashSet<String>()));
        Set<String> fav_names = new HashSet<>(sharedPreferences.getStringSet(tick_set_names, new HashSet<String>()));
        Set<String> port_shares = new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<String>()));
        String numshares = sharedPreferences.getString(num_of_shares,"");
        SharedPreferences.Editor edit_order_fav = sharedPreferences.edit();

        HashMap<String,String> port_map = new HashMap<>();
        String[] num_shares = numshares.split("_");

        if(port_shares.size() > 0){
            for(int i = 0; i<num_shares.length; ++i){
                String[] zs = num_shares[i].split("\\$");
                port_map.put(zs[0], zs[1]);
            }
        }

        if(favs.size() > 0){
            favview.setVisibility(View.VISIBLE);
            comp_names.clear();
            fav_tickers.clear();
            fav_changes.clear();
            fav_prices.clear();

            String fav_tick_lis = "";

            for(String t : favs){
                fav_tickers.add(t);
                fav_tick_lis = fav_tick_lis + t + ",";
            }

            String order_fav = sharedPreferences.getString(ORDER_FAV, "");
            if(order_fav.equals("") && favs.size() == 1){
                for(String i:favs)
                    order_fav = order_fav + i + "_";

                edit_order_fav.putString(ORDER_FAV, order_fav);
                edit_order_fav.commit();
            } else {
                String[] order_fav_array = order_fav.split("_");
                if(order_fav_array.length == favs.size()){
                    fav_tickers.clear();
                    for(int i = 0; i<order_fav_array.length; i++){
                        fav_tickers.add(order_fav_array[i]);
                    }
                }
                else {
                    for(String j:favs){
                        List<String> templ = new ArrayList<String>(Arrays.asList(order_fav_array));
                        if(!templ.contains(j)){
                            order_fav = order_fav + j + "_";
                            edit_order_fav.putString(ORDER_FAV, order_fav);
                            edit_order_fav.commit();
                            break;
                        }
                    }

                    String[] new_order_fav_array = order_fav.split("_");
                    fav_tickers.clear();
                    for(int i = 0; i<new_order_fav_array.length; i++){
                        fav_tickers.add(new_order_fav_array[i]);
                    }

                }
            }

            HashMap<String,String> temp_map_name = new HashMap<>();
            for(String t:fav_names){
                String[] tp = t.split("_");
                temp_map_name.put(tp[0], tp[1]);
            }

            for(int i = 0; i<fav_tickers.size(); ++i){
                if(port_shares.contains(fav_tickers.get(i))){
                    Double t = Double.parseDouble(port_map.get(fav_tickers.get(i)));
                    comp_names.add(String.format("%.2f", t) + " shares");
                }
                else
                    comp_names.add(temp_map_name.get(fav_tickers.get(i)));
            }


            JsonArrayRequest req_prices = new JsonArrayRequest(Request.Method.GET, url_lp + fav_tick_lis, null, new Response.Listener<JSONArray>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onResponse(JSONArray response) {
                    HashMap<String,String> temp_map_lp = new HashMap<>();
                    HashMap<String,String> temp_map_chg = new HashMap<>();

                    for(int i = 0; i<response.length(); ++i){
                        try {
                            JSONObject temp = response.getJSONObject(i);
                            double share_price = Double.parseDouble(temp.getString("last"));
                            temp_map_lp.put(temp.getString("ticker"), String.format("%.2f", share_price));

                            Double change = temp.getDouble("last") - temp.getDouble("prevClose");
                            temp_map_chg.put(temp.getString("ticker"), String.format("%.2f", change));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    for(int i = 0; i<fav_tickers.size(); ++i){
                        fav_prices.add(temp_map_lp.get(fav_tickers.get(i)));
                        fav_changes.add(temp_map_chg.get(fav_tickers.get(i)));
                    }


                    fadp.update_data(comp_names, fav_tickers, fav_prices, fav_changes);
                    fadp.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(req_prices);
        } else{
            favview.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(oncrt == false){
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String money = sharedPreferences.getString(cashflow, "20000.00");
            String port_order = sharedPreferences.getString(port_order_list, "");

            if(port_order.equals("")) {
                portview.setVisibility(View.GONE);
            }
            else{
                if(comptick.size() == 0) make_portfolio();
                else change_portfolio();
            }

            if(fav_tickers.size() == 0) make_favorites();
            else change_fav();
        }
        else{
            oncrt = !oncrt;
        }

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem =menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        ComponentName component = new ComponentName(this, DetailsActivity.class);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(component);
        searchView.setSearchableInfo(searchableInfo);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText(queryString);

            }
        });

        setupAutoSuggest(searchView);


        return true;
    }

    private void setupAutoSuggest(SearchView searchView) {
        final AppCompatAutoCompleteTextView autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        autoCompleteTextView.setDropDownHeight(1100);

        autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(3);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        fetchSuggestions(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void fetchSuggestions(String toString) {
        if(toString.length()<3) {
            autoSuggestAdapter.clearData();
            autoSuggestAdapter.notifyDataSetChanged();
            return;
        }

        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url+toString, null, response -> {
            List<String> stringList = new ArrayList<>();
            for(int i = 0; i<response.length(); ++i){
                try {
                    JSONObject temp = response.getJSONObject(i);
                    String name = temp.getString("name");
                    String ticker = temp.getString("ticker");
                    stringList.add(ticker + " - " + name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            autoSuggestAdapter.setData(stringList);
            autoSuggestAdapter.notifyDataSetChanged();
        }, error -> error.printStackTrace());

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10*1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);

    }
}