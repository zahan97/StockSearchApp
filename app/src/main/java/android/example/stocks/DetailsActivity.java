package android.example.stocks;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.Day;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DetailsActivity extends AppCompatActivity {

    String tpp1;

    Toolbar toolbar;
    String ticker;
    WebView highchart;

    TextView tick; TextView comp_name; TextView lp; TextView chg;
    TextView portfolio;
    TextView lp1; TextView low_p; TextView bid_p; TextView open_p; TextView mid_p; TextView high_p; TextView vol;
    TextView about; Boolean shorty = true ; Button vml;

    NestedScrollView details_layout;
    LinearLayout progspin;

    RecyclerView newslist;
    NewsAdapter newsAdapter;

    Button trade_button;

    public static final String tick_set = "tick_set";
    public static final String tick_set_names = "tick_set_names";
    public static final String tick_set_b = "tick_set_b";
    public static final String cashflow = "cashflow";
    public static final String num_of_shares = "num_of_shares";
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String port_order_list = "port_order_list";

    RequestQueue requestQueue;
    JSONObject latsp;
    JSONObject compdesc;
    JSONArray news; List<String> titles; List<String> sources; List<String> images; List<String> t_stamps; List<String> news_urls;
    String url_lp = "https://enduring-victor-294122.wn.r.appspot.com/latsp?symbol=";
    String url_desc = "https://enduring-victor-294122.wn.r.appspot.com/compdesc?symbol=";
    String url_news = "https://enduring-victor-294122.wn.r.appspot.com/news?symbol=";

    private MainActivity activity;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        Bundle temp =  getIntent().getExtras();
        if(temp != null){
            ticker = (String) temp.get("QUERY");
            tpp1 = "Dummy - dummy";
        }


        if(intent.ACTION_SEARCH.equals(intent.getAction())){
            tpp1 = intent.getStringExtra(SearchManager.QUERY);
            if(!tpp1.contains("-")){
                finish();
            }
            String[] tpp2 = tpp1.split(" ");
            ticker = tpp2[0];
        }

        requestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        activity = (MainActivity) getParent();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        details_layout = findViewById(R.id.detail_view);
        progspin = findViewById(R.id.proglayout);
        details_layout.setVisibility(View.INVISIBLE);

        Handler m = new Handler();
        m.postDelayed(new Runnable() {
            @Override
            public void run() {
                progspin.setVisibility(View.GONE);
                details_layout.setVisibility(View.VISIBLE);
            }
        },2000);


        //news_one = findViewById(R.id.news_one);

        tick = findViewById(R.id.tick);
        tick.setText(ticker);

        if(tpp1.contains("-")) {

            JsonObjectRequest desc_req = new JsonObjectRequest(Request.Method.GET, url_desc + ticker, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        compdesc = response;
                        comp_name = findViewById(R.id.comp_name);
                        comp_name.setText(compdesc.getString("name"));
                        about = findViewById(R.id.about);
                        about.setText(compdesc.getString("description"));
                        vml = findViewById(R.id.vml);
                        if (compdesc.getString("description").length() < 100) {
                            vml.setVisibility(View.INVISIBLE);
                        } else {
                            vml.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (shorty == true) {
                                        vml.setText("Show less");
                                        about.setEllipsize(null);
                                        about.setMaxLines(100);
                                        shorty = false;
                                    } else {
                                        vml.setText("Show more...");
                                        about.setMaxLines(2);
                                        about.setEllipsize(TextUtils.TruncateAt.END);
                                        shorty = true;
                                    }

                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            JsonArrayRequest lp_req = new JsonArrayRequest(Request.Method.GET, url_lp + ticker, null, new Response.Listener<JSONArray>() {
                @SuppressLint({"ResourceAsColor", "SetTextI18n"})
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        latsp = response.getJSONObject(0);
                        lp = findViewById(R.id.lp);
                        lp1 = findViewById(R.id.lp1);
                        open_p = findViewById(R.id.open_p);
                        high_p = findViewById(R.id.high_p);
                        vol = findViewById(R.id.vol);
                        low_p = findViewById(R.id.low_p);
                        mid_p = findViewById(R.id.mid_p);
                        bid_p = findViewById(R.id.bid_p);
                        chg = findViewById(R.id.chg);

                        lp.setText("$" + latsp.getString("last"));
                        lp1.setText(latsp.getString("last"));
                        open_p.setText(latsp.getString("open"));
                        high_p.setText(latsp.getString("high"));
                        String volume = NumberFormat.getNumberInstance(Locale.US).format(Long.parseLong(latsp.getString("volume"))) + ".00";
                        vol.setText(volume);
                        low_p.setText(latsp.getString("low"));
                        if (latsp.getString("mid") == "null") {
                            mid_p.setText("0.0");
                        } else {
                            mid_p.setText(latsp.getString("mid"));
                        }
                        if (latsp.getString("bidPrice") == "null" || latsp.getString("bidPrice") == "0") {
                            bid_p.setText("0.0");
                        } else {
                            bid_p.setText(latsp.getString("bidPrice"));
                        }
                        double diff = Double.parseDouble(latsp.getString("last")) - Double.parseDouble(latsp.getString("prevClose"));
                        String dif = String.format("%.2f", diff);
                        chg.setText("$" + dif);
                        if (diff < 0)
                            chg.setTextColor(ContextCompat.getColor(chg.getContext(), R.color.red));
                        else if (diff > 0)
                            chg.setTextColor(ContextCompat.getColor(chg.getContext(), R.color.green));

                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        Set<String> temp_tick =  sharedPreferences.getStringSet(tick_set_b, new HashSet<>());
                        String num_s = sharedPreferences.getString(num_of_shares, "");
                        String[] shares_ = num_s.split("_");
                        if(temp_tick.contains(ticker)){
                            for(String s: shares_){
                                if(s.contains(ticker)){
                                    String[] arr_ = s.split("\\$");
                                    Double share_num = Double.parseDouble(arr_[1]);
                                    Double market_Val = share_num * Double.parseDouble(latsp.getString("last"));
                                    portfolio.setText("     Shares Owned: " + String.format("%.4f", share_num) + "\nMarket Value: $" + String.format("%.2f", market_Val));
                                }
                            }
                        } else {
                            portfolio.setText("You have 0 shares of "+  ticker + ".\n             Start Trading!");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> error.printStackTrace());

            JsonObjectRequest news_req = new JsonObjectRequest(Request.Method.GET, url_news + ticker, null, new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        news = response.getJSONArray("news");
                        titles = new ArrayList<>();
                        sources = new ArrayList<>();
                        t_stamps = new ArrayList<>();
                        images = new ArrayList<>();
                        news_urls = new ArrayList<>();

                        for (int i = 0; i < news.length(); i++) {
                            JSONObject temp = news.getJSONObject(i);
                            titles.add(temp.getString("title"));
                            sources.add(temp.getJSONObject("source").getString("name"));
                            images.add(temp.getString("urlToImage"));

                            String tim = temp.getString("publishedAt");
                            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                            Date date = utcFormat.parse(tim);

                            Instant instant = Instant.now();

                            double start_date = date.getTime();
                            //double end_date = new Date().getTime();
                            double end_date = instant.toEpochMilli();

                            double days = (end_date - start_date)/(3600*24*1000);
                            long final_days = 0;
                            String ago = "";
                            if(days >= 1){
                                final_days = (long)days;
                                ago = final_days == 1 ? final_days + " day ago" : final_days + " days ago";
                            }
                            else{
                                days = (end_date - start_date)/(60*1000);
                                final_days = (long)days;
                                ago = final_days == 1 ? final_days + " minute ago" : final_days + " minutes ago";
                            }

                            t_stamps.add(ago);
                            news_urls.add(temp.getString("url"));
                        }

                        newslist = findViewById(R.id.newslist);
                        newsAdapter = new NewsAdapter(titles, sources, images, t_stamps, news_urls);
                        newslist.setAdapter(newsAdapter);
                        ViewCompat.setNestedScrollingEnabled(newslist, false);

                        TextView one_news_title, one_news_ts, one_news_desc;
                        one_news_title = findViewById(R.id.one_news_title);
                        one_news_desc = findViewById(R.id.one_news_desc);
                        one_news_ts = findViewById(R.id.one_news_ts);
                        ImageView news_one_image = findViewById(R.id.new_one_image);

                        one_news_title.setText(sources.get(0));
                        one_news_desc.setText(titles.get(0));
                        one_news_ts.setText(t_stamps.get(0));
                        Picasso.with(news_one_image.getContext()).load(images.get(0)).fit().centerCrop().into(news_one_image);

                        CardView news_one = findViewById(R.id.include2);

                        news_one.setOnClickListener(v -> {
                            String temp_url = news_urls.get(0);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url));
                            v.getContext().startActivity(browserIntent);
                        });

                        news_one.setOnLongClickListener(v -> {
                            Dialog dialog = new Dialog(v.getContext());
                            dialog.setContentView(R.layout.news_dialog);

                            ImageView dialog_img = dialog.findViewById(R.id.dialog_image);
                            Picasso.with(dialog_img.getContext()).load(images.get(0)).error(R.drawable.no_image_big).fit().centerCrop().into(dialog_img);

                            TextView dialog_text = dialog.findViewById(R.id.dialog_title);
                            dialog_text.setText(titles.get(0));

                            dialog.show();

                            ImageView dialog_twitter = dialog.findViewById(R.id.dialog_twitter);
                            ImageView dialog_chrome = dialog.findViewById(R.id.dialog_chrome);

                            dialog_twitter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String temp_url = news_urls.get(0);
                                    String temp_url_main = "https://twitter.com/intent/tweet?text=" + "Check out this Link: " + temp_url + "&hashtags=CSCI571StockApp";
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url_main));
                                    v.getContext().startActivity(browserIntent);
                                }
                            });

                            dialog_chrome.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String temp_url = news_urls.get(0);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp_url));
                                    v.getContext().startActivity(browserIntent);
                                }
                            });


                            return true;
                        });

                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> error.printStackTrace());

            requestQueue.add(desc_req);
            requestQueue.add(lp_req);
            requestQueue.add(news_req);
        }

        highchart = findViewById(R.id.webview);
        WebSettings webSettings = highchart.getSettings();
        webSettings.setJavaScriptEnabled(true);
        highchart.setWebViewClient(new Callback());
        highchart.loadUrl("https://zahan97.github.io/android.html?symbol="+ticker);
        highchart.setScrollbarFadingEnabled(true);
        highchart.setVerticalScrollBarEnabled(false);
        trade_button = findViewById(R.id.trade_button);
        portfolio = findViewById(R.id.portfolio);


        trade_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.trade_dialog);

                Dialog d2 = new Dialog(v.getContext());
                d2.setContentView(R.layout.trade_dialog_2);

                TextView d_cname = dialog.findViewById(R.id.d_cname);
                try {
                    d_cname.setText("Trade " + compdesc.getString("name") + " shares");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                EditText num_shares = dialog.findViewById(R.id.num_shares);
                TextView dynamic_cost = dialog.findViewById(R.id.dynamic_cost);
                TextView amt_available = dialog.findViewById(R.id.amt_available);

                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String cash_f = sharedPreferences.getString(cashflow, "20000.00");
                amt_available.setText("$"+cash_f+" available to buy "+ticker);

                try {
                    Double tprice = Double.parseDouble(latsp.getString("last"));
                    dynamic_cost.setText("0x$" + String.format("%.2f", tprice) + "/share = $0.0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                num_shares.setKeyListener(DigitsKeyListener.getInstance("0123456789,."));

                num_shares.addTextChangedListener(new TextWatcher() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        num_shares.setBackgroundTintList(ColorStateList.valueOf(R.color.purple_700));
                    }

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!num_shares.getText().toString().equals("") && !num_shares.getText().toString().equals(".") && !num_shares.getText().toString().matches("^[.]+[0-9]*$") && !num_shares.getText().toString().contains(",") && !num_shares.getText().toString().contains("-") && Double.parseDouble(num_shares.getText().toString()) > 0){
                            Double num = Double.parseDouble(num_shares.getText().toString());
                            try {
                                Double price = Double.parseDouble(latsp.getString("last"));
                                Double cost = num*price;
                                String tdc = Double.toString(num) + "x$" + String.format("%.2f", price) + "/share = $" + String.format("%.2f", cost);
                                dynamic_cost.setText(tdc);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Double tprice = Double.parseDouble(latsp.getString("last"));
                                dynamic_cost.setText("0x$" + String.format("%.2f", tprice) + "/share = $0.0");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                Button buy = dialog.findViewById(R.id.buy_button);
                Button sell = dialog.findViewById(R.id.sell_button);

                buy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!num_shares.getText().toString().equals("") && !num_shares.getText().toString().matches("^[.]+[0-9]*$") && !num_shares.getText().toString().equals(".") && !num_shares.getText().toString().contains(",") && !num_shares.getText().toString().contains("-") ){
                            if(Double.parseDouble(num_shares.getText().toString()) <= 0){
                                Toast.makeText(DetailsActivity.this, "Cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                Set<String> temp_tick =  new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<>()));
                                String port_order = sharedPreferences.getString(port_order_list, "");
                                String num_s = sharedPreferences.getString(num_of_shares, "");
                                String display_num_s = "";
                                Double cash = Double.parseDouble(sharedPreferences.getString(cashflow, "20000"));

                                if(temp_tick.contains(ticker)){
                                    String[] temp_num = num_s.split("_");
                                    String[] tpsn = new String[2];

                                    for(String s : temp_num){
                                        if(s.contains(ticker)){
                                            tpsn = s.split("\\$");
                                            break;
                                        }
                                    }

                                    display_num_s = tpsn[1];

                                    Double ns = Double.parseDouble(num_shares.getText().toString()) + Double.parseDouble(tpsn[1]);
                                    Double cost_incured;
                                    try {
                                        cost_incured = Double.parseDouble(num_shares.getText().toString())*Double.parseDouble(latsp.getString("last"));

                                        if(cost_incured > cash){
                                            Toast.makeText(DetailsActivity.this, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                                        } else{
                                            display_num_s = String.format("%.4f", ns);

                                            String final_str = "";

                                            for(String s : temp_num){
                                                if(s.contains(ticker)){
                                                    tpsn = s.split("\\$");
                                                    final_str = final_str + tpsn[0] + "$" + ns.toString() + "_";
                                                } else{
                                                    final_str = final_str + s + "_";
                                                }
                                            }

                                            cash = cash - cost_incured;
                                            editor.putString(cashflow, String.format("%.2f", cash));
                                            //Log.v("IMP", String.format("%.2f", cash));
                                            editor.putString(num_of_shares, final_str);

                                            editor.commit();
                                            dialog.dismiss();

                                            TextView success_txt = d2.findViewById(R.id.success_txt);
                                            success_txt.setText("You have successfully bought "+num_shares.getText().toString()+"\nshares of "+ticker);
                                            Button dismis_but = d2.findViewById(R.id.dismiss_but);
                                            d2.show();

                                            dismis_but.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    d2.dismiss();
                                                }
                                            });

                                            try {
                                                Double market_Val = Double.parseDouble(display_num_s) * Double.parseDouble(latsp.getString("last"));
                                                portfolio.setText("     Shares Owned: " + display_num_s + "\nMarket Value: $" + String.format("%.2f", market_Val));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    temp_tick.add(ticker);
                                    editor.putStringSet(tick_set_b,temp_tick);

                                    port_order = port_order + ticker + "_";
                                    editor.putString(port_order_list, port_order);

                                    Double to = Double.parseDouble(num_shares.getText().toString());
                                    try {
                                        Double price = Double.parseDouble(latsp.getString("last"));
                                        if(to*price > cash){
                                            Toast.makeText(DetailsActivity.this, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                                        } else{
                                            display_num_s = String.format("%.4f", to);

                                            cash = cash - (to*price);
                                            editor.putString(cashflow, String.format("%.2f", cash));
                                            //Log.v("IMP", String.format("%.2f", cash));
                                            num_s = num_s + ticker + "$" + num_shares.getText().toString() + "_";
                                            editor.putString(num_of_shares, num_s);
                                            editor.commit();
                                            dialog.dismiss();

                                            TextView success_txt = d2.findViewById(R.id.success_txt);
                                            success_txt.setText("You have successfully bought "+num_shares.getText().toString()+"\nshares of "+ticker);
                                            Button dismis_but = d2.findViewById(R.id.dismiss_but);
                                            d2.show();

                                            dismis_but.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    d2.dismiss();
                                                }
                                            });

                                            try {
                                                Double market_Val = Double.parseDouble(display_num_s) * Double.parseDouble(latsp.getString("last"));
                                                portfolio.setText("     Shares Owned: " + display_num_s + "\nMarket Value: $" + String.format("%.2f", market_Val));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                /*
                                try {
                                    Double market_Val = Double.parseDouble(display_num_s) * Double.parseDouble(latsp.getString("last"));
                                    portfolio.setText("     Shares Owned: " + display_num_s + "\nMarket Value: $" + String.format("%.2f", market_Val));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }*/

                            }

                        } else {
                            Toast.makeText(DetailsActivity.this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                sell.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String port_order = sharedPreferences.getString(port_order_list, "");

                        Set<String> ts = new HashSet<>(sharedPreferences.getStringSet(tick_set_b, new HashSet<>()));
                        String number_ofs = sharedPreferences.getString(num_of_shares, "");

                        Double cash = Double.parseDouble(sharedPreferences.getString(cashflow, "20000"));

                        if(!num_shares.getText().toString().equals("") && !num_shares.getText().toString().matches("^[.]+[0-9]*$") && !num_shares.getText().toString().equals(".") && !num_shares.getText().toString().contains(",") && !num_shares.getText().toString().contains("-")){
                            if(Double.parseDouble(num_shares.getText().toString()) <= 0){
                                Toast.makeText(DetailsActivity.this, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                            } else {
                                if(ts.contains(ticker)){
                                    String[] temp_num = number_ofs.split("_");
                                    String[] tpsn = new String[2];

                                    for(String s : temp_num){
                                        if(s.contains(ticker)){
                                            tpsn = s.split("\\$");
                                            break;
                                        }
                                    }

                                    if(Double.parseDouble(num_shares.getText().toString()) <= Double.parseDouble(tpsn[1])){
                                        Double new_shares_amt = Double.parseDouble(tpsn[1]) - Double.parseDouble(num_shares.getText().toString());
                                        String final_str = "";
                                        if(new_shares_amt == 0){
                                            ts.remove(ticker);
                                            for(String s : temp_num){
                                                if(s.contains(ticker)){
                                                    continue;
                                                } else{
                                                    final_str = final_str + s + "_";
                                                }
                                            }

                                            if(ts.size() != 0){
                                                String[] tporder = port_order.split("_");
                                                String tpol = "";
                                                for(int i = 0; i<tporder.length; ++i){
                                                    if(!tporder[i].equals(ticker)){
                                                        tpol = tpol + tporder[i] + "_";
                                                    }
                                                }

                                                editor.putString(port_order_list, tpol);
                                            } else {
                                                editor.putString(port_order_list, "");
                                            }


                                            editor.putStringSet(tick_set_b, ts);
                                        } else {
                                            for(String s : temp_num){
                                                if(s.contains(ticker)){
                                                    tpsn = s.split("\\$");
                                                    final_str = final_str + tpsn[0] + "$" + new_shares_amt.toString() + "_";
                                                } else{
                                                    final_str = final_str + s + "_";
                                                }
                                            }
                                        }

                                        try {
                                            cash = cash + Double.parseDouble(num_shares.getText().toString())*Double.parseDouble(latsp.getString("last"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        editor.putString(num_of_shares, final_str);
                                        editor.putString(cashflow, String.format("%.2f", cash));

                                        editor.commit();
                                        dialog.dismiss();

                                        TextView success_txt = d2.findViewById(R.id.success_txt);
                                        success_txt.setText("You have successfully sold "+num_shares.getText().toString()+" shares\nof "+ticker);
                                        Button dismis_but = d2.findViewById(R.id.dismiss_but);
                                        d2.show();

                                        dismis_but.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                d2.dismiss();
                                            }
                                        });

                                        try {
                                            Double market_Val = new_shares_amt * Double.parseDouble(latsp.getString("last"));
                                            if(new_shares_amt != 0){
                                                portfolio.setText("     Shares Owned: " + String.format("%.4f", new_shares_amt) + "\nMarket Value: $" + String.format("%.2f", market_Val));
                                            } else {
                                                portfolio.setText("You have 0 shares of "+  ticker + ".\n             Start Trading!");
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        Toast.makeText(DetailsActivity.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(DetailsActivity.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(DetailsActivity.this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                dialog.show();
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.ACTION_SEARCH.equals(intent.getAction())){
            String tpp1 = intent.getStringExtra(SearchManager.QUERY);
            String[] tpp2 = tpp1.split(" ");
            ticker = tpp2[0];
            finish();
        }
    }*/

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.star);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> temp_tick = new HashSet<>(sharedPreferences.getStringSet(tick_set, new HashSet<String>()));

        if(temp_tick.contains(ticker)){
            menuItem.setIcon(R.drawable.ic_baseline_star_24);
        } else {
            menuItem.setIcon(R.drawable.ic_baseline_star_border_24);
        }

        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Set<String> temp_tick = new HashSet<>(sharedPreferences.getStringSet(tick_set, new HashSet<String>()));
                Set<String> temp_tick_names = new HashSet<>(sharedPreferences.getStringSet(tick_set_names, new HashSet<String>()));
                String fav_order = sharedPreferences.getString("ORDER_FAV", "");

                String cname = "";
                try {
                    cname = compdesc.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(temp_tick.contains(ticker)){
                    menuItem.setIcon(R.drawable.ic_baseline_star_border_24);
                    temp_tick.remove(ticker);
                    temp_tick_names.remove(ticker+"_"+cname);

                    String[] temp = fav_order.split("_");
                    List<String> templ = new ArrayList<String>(Arrays.asList(temp));


                    int posi = 0;
                    for(int i = 0; i<templ.size(); ++i){
                        if(templ.get(i).equals(ticker)){
                            posi = i;
                            break;
                        }
                    }

                    templ.remove(posi);
                    fav_order = "";
                    for(String j:templ){
                        fav_order = fav_order + j + "_";
                    }

                    Log.v("IMP1","here"+fav_order);
                    editor.putString("ORDER_FAV", fav_order);


                    editor.putStringSet(tick_set, temp_tick);
                    editor.putStringSet(tick_set_names, temp_tick_names);
                    editor.commit();

                    Toast.makeText(DetailsActivity.this, "\"" + ticker + "\" was removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    menuItem.setIcon(R.drawable.ic_baseline_star_24);
                    temp_tick.add(ticker);

                    temp_tick_names.add(ticker+"_"+cname);
                    editor.putStringSet(tick_set, temp_tick);
                    editor.putStringSet(tick_set_names, temp_tick_names);
                    editor.commit();
                    Toast.makeText(DetailsActivity.this, "\"" + ticker + "\" was added to favorites", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        return true;
    }
}