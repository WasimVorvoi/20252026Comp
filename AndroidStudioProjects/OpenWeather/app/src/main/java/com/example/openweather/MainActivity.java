package com.example.openweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    String apiKey = "9c75ed29a61c0a33b6bbe729016d429c";
    EditText etZipCode;
    Button btnSearch;
    View weatherCard, forecastCard;
    TextView tvCity, tvDateTime, tvTemp, tvCondition, tvHighLow, tvFeelsLike, tvPrecip, tvSunInfo;
    TextView tvDay1, tvFIcon1, tvFDesc1, tvFTemps1, tvFTime1;
    TextView tvDay2, tvFIcon2, tvFDesc2, tvFTemps2, tvFTime2;
    TextView tvDay3, tvFIcon3, tvFDesc3, tvFTemps3, tvFTime3;
    TextView tvDay4, tvFIcon4, tvFDesc4, tvFTemps4, tvFTime4;
    TextView tvDay5, tvFIcon5, tvFDesc5, tvFTemps5, tvFTime5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etZipCode = findViewById(R.id.etZipCode);
        btnSearch = findViewById(R.id.btnSearch);
        weatherCard = findViewById(R.id.weatherCard);
        forecastCard = findViewById(R.id.forecastCard);
        tvCity = findViewById(R.id.tvCity);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        tvHighLow = findViewById(R.id.tvHighLow);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvPrecip = findViewById(R.id.tvPrecip);
        tvSunInfo = findViewById(R.id.tvSunInfo);
        tvDay1 = findViewById(R.id.tvDay1); tvFIcon1 = findViewById(R.id.tvFIcon1);
        tvFDesc1 = findViewById(R.id.tvFDesc1); tvFTemps1 = findViewById(R.id.tvFTemps1); tvFTime1 = findViewById(R.id.tvFTime1);
        tvDay2 = findViewById(R.id.tvDay2); tvFIcon2 = findViewById(R.id.tvFIcon2);
        tvFDesc2 = findViewById(R.id.tvFDesc2); tvFTemps2 = findViewById(R.id.tvFTemps2); tvFTime2 = findViewById(R.id.tvFTime2);
        tvDay3 = findViewById(R.id.tvDay3); tvFIcon3 = findViewById(R.id.tvFIcon3);
        tvFDesc3 = findViewById(R.id.tvFDesc3); tvFTemps3 = findViewById(R.id.tvFTemps3); tvFTime3 = findViewById(R.id.tvFTime3);
        tvDay4 = findViewById(R.id.tvDay4); tvFIcon4 = findViewById(R.id.tvFIcon4);
        tvFDesc4 = findViewById(R.id.tvFDesc4); tvFTemps4 = findViewById(R.id.tvFTemps4); tvFTime4 = findViewById(R.id.tvFTime4);
        tvDay5 = findViewById(R.id.tvDay5); tvFIcon5 = findViewById(R.id.tvFIcon5);
        tvFDesc5 = findViewById(R.id.tvFDesc5); tvFTemps5 = findViewById(R.id.tvFTemps5); tvFTime5 = findViewById(R.id.tvFTime5);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zip = etZipCode.getText().toString().trim();
                if (!zip.isEmpty()) {
                    AsyncThread task = new AsyncThread();
                    task.execute(zip);
                }
            }
        });
    }

    public class AsyncThread extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String zip = strings[0];
            try {
                URL geoUrl = new URL("https://api.openweathermap.org/geo/1.0/zip?zip=" + zip + ",us&appid=" + apiKey);
                URLConnection geoConnection = geoUrl.openConnection();
                InputStream geoStream = geoConnection.getInputStream();
                BufferedReader geoReader = new BufferedReader(new InputStreamReader(geoStream));
                String line;
                String geoData = "";
                while ((line = geoReader.readLine()) != null) {
                    geoData = geoData + line;
                }
                Log.d("TAG", geoData);

                JSONObject geoJson = new JSONObject(geoData);
                double lat = geoJson.getDouble("lat");
                double lon = geoJson.getDouble("lon");
                String name = geoJson.getString("name");

                URL weatherUrl = new URL("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=imperial&appid=" + apiKey);
                URLConnection weatherConnection = weatherUrl.openConnection();
                InputStream weatherStream = weatherConnection.getInputStream();
                BufferedReader weatherReader = new BufferedReader(new InputStreamReader(weatherStream));
                String weatherLine;
                String weatherData = "";
                while ((weatherLine = weatherReader.readLine()) != null) {
                    weatherData = weatherData + weatherLine;
                }
                Log.d("TAG", weatherData);

                JSONObject forecastJson = new JSONObject(weatherData);
                JSONArray list = forecastJson.getJSONArray("list");

                TimeZone est = TimeZone.getTimeZone("America/New_York");
                SimpleDateFormat dayDateFmt = new SimpleDateFormat("EEEE, MMM d");
                dayDateFmt.setTimeZone(est);
                String dayDate = dayDateFmt.format(new Date());

                SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
                timeFmt.setTimeZone(est);
                String currentTime = timeFmt.format(new Date());

                SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
                dateFmt.setTimeZone(est);
                String today = dateFmt.format(new Date());

                double highTemp = -999;
                double lowTemp = 999;
                double highfeel = -999;
                double lowfeel = 999;
                double currentTemp = 0;
                double maxPop = 0;
                String condition = "N/A";
                boolean firstEntry = true;

                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    String entryDate = entry.getString("dt_txt").split(" ")[0];
                    if (entryDate.equals(today)) {
                        JSONObject main = entry.getJSONObject("main");
                        double maxtemp = main.getDouble("temp_max");
                        double mintemp = main.getDouble("temp_min");
                        double feel = main.getDouble("feels_like");
                        double temp = main.getDouble("temp");
                        double pop = entry.optDouble("pop", 0.0);
                        if (firstEntry) {
                            currentTemp = temp;
                            JSONArray weatherArr = entry.getJSONArray("weather");
                            condition = weatherArr.getJSONObject(0).getString("description").toUpperCase();
                            firstEntry = false;
                        }
                        if (maxtemp > highTemp) highTemp = maxtemp;
                        if (mintemp < lowTemp) lowTemp = mintemp;
                        if (feel > highfeel) highfeel = feel;
                        if (feel < lowfeel) lowfeel = feel;
                        if (pop > maxPop) maxPop = pop;
                    }
                }

                JSONObject city = forecastJson.getJSONObject("city");
                long sunriseUnix = city.getLong("sunrise");
                long sunsetUnix = city.getLong("sunset");
                SimpleDateFormat sunFmt = new SimpleDateFormat("h:mm a");
                sunFmt.setTimeZone(est);
                String sunriseStr = sunFmt.format(new Date(sunriseUnix * 1000L));
                String sunsetStr = sunFmt.format(new Date(sunsetUnix * 1000L));
                int precipPct = (int) Math.round(maxPop * 100);

                String mainResult = name + "|||" + dayDate + " \u2022 " + currentTime + "|||" + condition + "|||"
                        + Math.round(currentTemp) + "|||" + Math.round(highTemp) + "|||"
                        + Math.round(lowTemp) + "|||" + Math.round(lowfeel) + "|||"
                        + Math.round(highfeel) + "|||" + precipPct + "|||" + sunriseStr + "|||" + sunsetStr;

                String[] forecastDates = new String[5];
                JSONObject[] noonEntries = new JSONObject[5];
                double[] forecastHighs = new double[]{-999, -999, -999, -999, -999};
                double[] forecastLows = new double[]{999, 999, 999, 999, 999};
                int forecastCount = 0;

                SimpleDateFormat fullDtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                fullDtFmt.setTimeZone(est);
                SimpleDateFormat displayTimeFmt = new SimpleDateFormat("h:mm a");
                displayTimeFmt.setTimeZone(est);
                SimpleDateFormat parseDateFmt = new SimpleDateFormat("yyyy-MM-dd");
                parseDateFmt.setTimeZone(est);
                SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE");
                dayNameFmt.setTimeZone(est);

                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    String dtTxt = entry.getString("dt_txt");
                    String entryDate = dtTxt.split(" ")[0];
                    String entryTime = dtTxt.split(" ")[1];

                    int dateIdx = -1;
                    for (int j = 0; j < forecastCount; j++) {
                        if (forecastDates[j].equals(entryDate)) {
                            dateIdx = j;
                            break;
                        }
                    }

                    if (dateIdx == -1 && forecastCount < 5) {
                        forecastDates[forecastCount] = entryDate;
                        noonEntries[forecastCount] = entry;
                        dateIdx = forecastCount;
                        forecastCount++;
                    }

                    if (dateIdx >= 0) {
                        if (entryTime.equals("12:00:00")) {
                            noonEntries[dateIdx] = entry;
                        }
                        JSONObject main = entry.getJSONObject("main");
                        double tempMax = main.getDouble("temp_max");
                        double tempMin = main.getDouble("temp_min");
                        if (tempMax > forecastHighs[dateIdx]) forecastHighs[dateIdx] = tempMax;
                        if (tempMin < forecastLows[dateIdx]) forecastLows[dateIdx] = tempMin;
                    }
                }

                String forecastResult = "";
                for (int i = 0; i < forecastCount; i++) {
                    JSONObject noonEntry = noonEntries[i];
                    String dtTxt = noonEntry.getString("dt_txt");
                    Date parsedDate = parseDateFmt.parse(dtTxt.split(" ")[0]);
                    String dayName = dayNameFmt.format(parsedDate);

                    JSONArray weatherArr = noonEntry.getJSONArray("weather");
                    String desc = weatherArr.getJSONObject(0).getString("description").toUpperCase();
                    String icon = weatherArr.getJSONObject(0).getString("icon");

                    String ic = icon.substring(0, 2);
                    String emoji;
                    if (ic.equals("01")) emoji = "\u2600\uFE0F";
                    else if (ic.equals("02")) emoji = "\u26C5";
                    else if (ic.equals("03") || ic.equals("04")) emoji = "\u2601\uFE0F";
                    else if (ic.equals("09")) emoji = "\uD83C\uDF27\uFE0F";
                    else if (ic.equals("10")) emoji = "\uD83C\uDF26\uFE0F";
                    else if (ic.equals("11")) emoji = "\u26C8\uFE0F";
                    else if (ic.equals("13")) emoji = "\uD83C\uDF28\uFE0F";
                    else if (ic.equals("50")) emoji = "\uD83C\uDF2B\uFE0F";
                    else emoji = "\uD83C\uDF24\uFE0F";

                    Date fullDt = fullDtFmt.parse(dtTxt);
                    String displayTime = displayTimeFmt.format(fullDt);

                    if (i > 0) forecastResult += "~~~";
                    forecastResult += dayName + "^" + emoji + "^" + desc + "^"
                            + Math.round(forecastHighs[i]) + "^" + Math.round(forecastLows[i]) + "^" + displayTime;
                }

                return mainResult + "===" + forecastResult;

            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Error")) {
                tvCity.setText("Invalid ZIP code");
                weatherCard.setVisibility(View.VISIBLE);
                return;
            }

            String[] mainAndForecast = result.split("===");
            String[] parts = mainAndForecast[0].split("\\|\\|\\|");
            tvCity.setText(parts[0]);
            tvDateTime.setText(parts[1]);
            tvTemp.setText(parts[3] + "\u00B0F");
            tvCondition.setText(parts[2]);
            tvHighLow.setText("H: " + parts[4] + "\u00B0F   \u2022   L: " + parts[5] + "\u00B0F");
            tvFeelsLike.setText("Feels like " + parts[6] + "\u00B0F \u2013 " + parts[7] + "\u00B0F");
            tvPrecip.setText("\uD83C\uDF27  Chance of Rain: " + parts[8] + "%");
            tvSunInfo.setText("\uD83C\uDF05  Sunrise: " + parts[9] + "     \uD83C\uDF07  Sunset: " + parts[10]);
            weatherCard.setVisibility(View.VISIBLE);

            String[] days = mainAndForecast[1].split("~~~");
            TextView[][] rows = {
                {tvDay1, tvFIcon1, tvFDesc1, tvFTemps1, tvFTime1},
                {tvDay2, tvFIcon2, tvFDesc2, tvFTemps2, tvFTime2},
                {tvDay3, tvFIcon3, tvFDesc3, tvFTemps3, tvFTime3},
                {tvDay4, tvFIcon4, tvFDesc4, tvFTemps4, tvFTime4},
                {tvDay5, tvFIcon5, tvFDesc5, tvFTemps5, tvFTime5}
            };

            for (int i = 0; i < days.length && i < 5; i++) {
                String[] f = days[i].split("\\^");
                rows[i][0].setText(f[0]);
                rows[i][1].setText(f[1]);
                rows[i][2].setText(f[2]);
                rows[i][3].setText("H: " + f[3] + "\u00B0  L: " + f[4] + "\u00B0");
                rows[i][4].setText(f[5]);
            }

            forecastCard.setVisibility(View.VISIBLE);
        }
    }
}
