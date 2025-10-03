package aman.first.weatherapp;

import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText enteryourcity;
    Button cheakweather;
    TextView yourresult;

    private static final String API_KEY = "317eba995135b909ca41a22d50a6feeb";
    OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);hashCode();
        setContentView(R.layout.activity_main);
        enteryourcity = findViewById(R.id.city);
        cheakweather = findViewById(R.id.cheakweather);
        yourresult = findViewById(R.id.text);

        okHttpClient = new OkHttpClient();
        cheakweather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = enteryourcity.getText().toString().trim();
                if (city == null && city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your city", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchcitywether(city);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void fetchcitywether(String city){
        String encodedcity = null  ;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            encodedcity= URLEncoder.encode(city , StandardCharsets.UTF_8);
        }
        String baseurl = "https://api.openweathermap.org/data/2.5/weather?q="+encodedcity + "&appid=" + API_KEY + "&units=metric";
        Request request = new Request.Builder().url(baseurl).get().build();
        yourresult.setText("Loading....");
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(()->yourresult.setText("Error: "));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()){
                    runOnUiThread(()->yourresult.setText("Error"+response.code()));
                    return;
                }
                String responsebody = response.body().string();
                try {
                    JSONObject root = new JSONObject(responsebody);
                    String name = root.optString("name", "Nothing");

                    JSONObject coordinate = root.optJSONObject("coord");
                    double lat = coordinate != null ? coordinate.optDouble("lat", 0) : 0;
                    double lon = coordinate != null ? coordinate.optDouble("lon", 0) : 0;

                    JSONObject main = root.optJSONObject("main");
                    double temp = main != null ? main.optDouble("temp", Double.NaN) : Double.NaN;
                    double tempmin = main != null ? main.optDouble("temp_min", Double.NaN) : Double.NaN;
                    double tempmax = main != null ? main.optDouble("temp_max", Double.NaN) : Double.NaN;
                    double feelslike = main != null ? main.optDouble("feels_like", Double.NaN) : Double.NaN;
                    int pressure = main != null ? main.optInt("pressure", -1) : -1;
                    int humidity = main != null ? main.optInt("humidity", -1) : -1;
                    int sea_level = main != null ? main.optInt("sea_level", -1) : -1;
                    int grnd_level = main != null ? main.optInt("grnd_level", -1) : -1;

                    JSONObject wind = root.optJSONObject("wind");
                    double speed = wind != null ? wind.optDouble("speed", Double.NaN) : Double.NaN;
                    int deg = wind != null ? wind.optInt("deg", -1) : -1;
                    double gust = wind != null ? wind.optDouble("gust", Double.NaN) : Double.NaN;

                    JSONObject sys = root.optJSONObject("sys");
                    String country = sys.optString("country", "Nothing");
                    String updteddate = "Lattitude:"+lat+"Longitude:"+lon;
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String countryName = address.getCountryName();
                            String cityName = address.getLocality();
                            String stateName = address.getAdminArea();

                            updteddate = countryName + (cityName != null ? (", " + cityName) : "");
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    int sunrise = sys != null ? sys.optInt("sunrise", -1) : -1;
                    int sunset = sys != null ? sys.optInt("sunset", -1) : -1;

                    JSONObject clouds = root.optJSONObject("cloud");
                    int all = clouds != null ? clouds.optInt("all", -1) : -1;

                    int timezone = root.optInt("timezone", -1);
                    int id = root.optInt("id", -1);
                    int cod = root.optInt("cod", -1);


                    final String weatherdata =
                            "\n id:     " + id +
                                    "\n cod:    " + cod +
                                    "\n all:    " + all +
                                    "\nspeed:   " + speed +
                                    "\n deg:    " + deg +
                                    "\n gust:   " + gust +
                                    "\n sunset:     " + sunset +
                                    "\n country:    " + updteddate +
                                    "\n sunrise:    " + sunrise +
                                    "\n timezone:   " + timezone +
                                    "\n Temprature: " + temp + "°C" +
                                    "\n feels_like: " + feelslike + "F" +
                                    "\n pressure:   " + pressure +
                                    "\n humidity:   " + humidity +
                                    "\n sea_level:  " + sea_level +
                                    "\n grnd_level: " + grnd_level +
                                    "\nMinimum Temprature:  " + tempmin + "°C" +
                                    "\n Maximum Temprature: " + tempmax + "°C" +
                                    "Coordinate:" + lat + "," + lon;

                    runOnUiThread(() -> yourresult.setText(weatherdata));


                }catch (Exception e){
                    runOnUiThread(()->yourresult.setText("Error in response Body" + e.getMessage()));
                }
            }
        });
    }
}



