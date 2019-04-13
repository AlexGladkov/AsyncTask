package com.agladkov.javasandbox;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextView txtResults;
    private EditText textSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResults = findViewById(R.id.txtResultsCount);
        textSearch = findViewById(R.id.textState);
        Button btnSearch = findViewById(R.id.btnSearch);

        setResultsCount(0);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GitTask gitTask = new GitTask();
                gitTask.setMainActivity(new WeakReference<>(MainActivity.this));
                gitTask.execute(textSearch.getText().toString());
            }
        });
    }

    private void setResultsCount(Integer count) {
        txtResults.setText(getString(R.string.results).replace("[Count]", count.toString()));
    }

    static class GitTask extends AsyncTask<String, Integer, Integer> {
        private String TAG = MainActivity.class.getCanonicalName();
        private OkHttpClient okHttpClient;
        private Retrofit retrofit;
        private LyricsService service;
        private WeakReference<MainActivity> mainActivity;

        void setMainActivity(WeakReference<MainActivity> mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://chroniclingamerica.loc.gov/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(LyricsService.class);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Call<JsonObject> request = service.listLyrics(strings[0], "json");

            try {
                Response<JsonObject> response = request.execute();
                JsonObject result = response.body();
                return result.get("totalItems").getAsInt();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            try {
                mainActivity.get().setResultsCount(integer);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
