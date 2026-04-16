package es.tatvil.dev;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatosFragment extends Fragment {

    private static final String TAG = "DatosFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView tvFecha, tvNombre, tvUbicacion, tvClima, tvSantoDelDia, tvPrediccionHoy, tvPrediccionManana;
    private final OkHttpClient client = new OkHttpClient();
    private final String API_KEY = "69ef7f26726bba12b03c74b1e97b550f";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_datos_fragment, container, false);

        tvFecha = view.findViewById(R.id.tvFecha);
        tvNombre = view.findViewById(R.id.tvNombreUsuario);
        tvUbicacion = view.findViewById(R.id.tvUbicacion);
        tvClima = view.findViewById(R.id.tvClima);
        tvSantoDelDia = view.findViewById(R.id.tvSantodeldia);
        tvPrediccionHoy = view.findViewById(R.id.tvPrediccionHoy);
        tvPrediccionManana = view.findViewById(R.id.tvPrediccionManana);

        cargarDatosFijos();
        solicitarPermisoUbicacion();

        return view;
    }

    private void cargarDatosFijos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            String fechaFormateada = hoy.format(formato);
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase(new Locale("es", "ES")) + fechaFormateada.substring(1);
            tvFecha.setText(fechaFormateada);
            obtenerSantoDelDia(hoy.getDayOfMonth(), hoy.getMonthValue());
        }
    }

    private void solicitarPermisoUbicacion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            busquedaUbicacion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            busquedaUbicacion();
        }
    }

    public void busquedaUbicacion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                executor.execute(() -> {
                    String pueblo = obtenerNombrePueblo(location.getLatitude(), location.getLongitude());
                    handler.post(() -> {
                        if (pueblo != null) {
                            tvUbicacion.setText(pueblo);
                            obtenerClimaYPrediccion(pueblo);
                        }
                    });
                });
            }
        });
    }

    private String obtenerNombrePueblo(double lat, double lon) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) return direcciones.get(0).getLocality();
        } catch (IOException e) { Log.e(TAG, "Error Geocoder", e); }
        return null;
    }

    private void obtenerClimaYPrediccion(String ciudad) {
        // 1. Clima Actual
        String urlActual = "https://api.openweathermap.org/data/2.5/weather?q=" + ciudad + "&units=metric&lang=es&appid=" + API_KEY;
        Request requestActual = new Request.Builder().url(urlActual).build();
        client.newCall(requestActual).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error clima actual", e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        double temp = json.getJSONObject("main").getDouble("temp");
                        String desc = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        handler.post(() -> tvClima.setText("Ahora: " + Math.round(temp) + "°C, " + desc));
                    } catch (Exception e) { Log.e(TAG, "Error JSON clima actual", e); }
                }
            }
        });

        // 2. Predicción Hoy y Mañana
        String urlForecast = "https://api.openweathermap.org/data/2.5/forecast?q=" + ciudad + "&units=metric&lang=es&appid=" + API_KEY;
        Request requestForecast = new Request.Builder().url(urlForecast).build();
        client.newCall(requestForecast).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error predicción", e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray list = json.getJSONArray("list");

                        // Para hoy (máx/mín de las próximas 12 horas)
                        double minHoy = Double.MAX_VALUE;
                        double maxHoy = Double.MIN_VALUE;
                        for (int i = 0; i < 4; i++) { // Próximas 12h (3h * 4)
                            JSONObject obj = list.getJSONObject(i).getJSONObject("main");
                            minHoy = Math.min(minHoy, obj.getDouble("temp_min"));
                            maxHoy = Math.max(maxHoy, obj.getDouble("temp_max"));
                        }

                        // Para mañana (+24h aprox, índices 8 a 15)
                        double minMan = Double.MAX_VALUE;
                        double maxMan = Double.MIN_VALUE;
                        for (int i = 8; i < 16; i++) {
                            JSONObject obj = list.getJSONObject(i).getJSONObject("main");
                            minMan = Math.min(minMan, obj.getDouble("temp_min"));
                            maxMan = Math.max(maxMan, obj.getDouble("temp_max"));
                        }

                        double finalMinHoy = minHoy;
                        double finalMaxHoy = maxHoy;
                        double finalMinMan = minMan;
                        double finalMaxMan = maxMan;

                        handler.post(() -> {
                            tvPrediccionHoy.setText("Hoy: m" + Math.round(finalMinHoy) + "° / M" + Math.round(finalMaxHoy) + "°");
                            tvPrediccionManana.setText("Mañana: m" + Math.round(finalMinMan) + "° / M" + Math.round(finalMaxMan) + "°");
                        });
                    } catch (Exception e) { Log.e(TAG, "Error JSON predicción", e); }
                }
            }
        });
    }

    private void obtenerSantoDelDia(int dia, int mes) {
        executor.execute(() -> {
            String santo = "Santo del día: Desconocido";
            try (XmlResourceParser parser = getResources().getXml(R.xml.santos)) {
                int eventType = parser.getEventType();
                while (eventType != XmlResourceParser.END_DOCUMENT) {
                    if (eventType == XmlResourceParser.START_TAG && "dia".equals(parser.getName())) {
                        if (mes == Integer.parseInt(parser.getAttributeValue(null, "mes")) &&
                            dia == Integer.parseInt(parser.getAttributeValue(null, "numero"))) {
                            santo = parser.getAttributeValue(null, "santo");
                            break;
                        }
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) { Log.e(TAG, "Error XML", e); }
            String finalSanto = santo;
            handler.post(() -> tvSantoDelDia.setText(finalSanto));
        });
    }
}
