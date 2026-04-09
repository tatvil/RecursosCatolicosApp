package es.tatvil.recursoscatolicos;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView fechaTextView;
    private TextView santoDelDiaTextView;
    private TextView quoteTextView;
    private TextView quoteAuthorTextView;
    private CardView btnBiblia;
    private CardView btnRosario;
    private CardView btnDiario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicialización de Vistas
        fechaTextView = findViewById(R.id.fecha);
        santoDelDiaTextView = findViewById(R.id.santodeldia);
        btnBiblia = findViewById(R.id.btn_biblia_card);
        btnRosario = findViewById(R.id.btn_rosario_card);
        btnDiario = findViewById(R.id.journal_card);
        quoteTextView = findViewById(R.id.quote_text);
        quoteAuthorTextView = findViewById(R.id.quote_author);

        // 2. Configuración de la Cabecera
        configurarFechaYSanto();
        configurarSalmoDelDia();

        // 3. Configuración de Listeners
        configurarNavegacion();
    }

    private void configurarSalmoDelDia() {
        LocalDate hoy = LocalDate.now();
        // Usamos el día del año para rotar los 150 salmos
        int diaDelAno = hoy.getDayOfYear();
        int numeroSalmo = (diaDelAno % 150) + 1;

        obtenerSalmoAsync(numeroSalmo);
    }

    private void obtenerSalmoAsync(int numeroSalmo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String[] resultado = obtenerSalmoSincrono(numeroSalmo);
            handler.post(() -> {
                quoteTextView.setText(resultado[0]);
                quoteAuthorTextView.setText(resultado[1]);
            });
        });
    }

    private String[] obtenerSalmoSincrono(int numero) {
        String textoSalmo = "Cargando salmo...";
        String autorSalmo = "Salmo " + numero;
        try (XmlResourceParser parser = getResources().getXml(R.xml.salmos)) {
            int eventType = parser.getEventType();
            boolean salmoEncontrado = false;
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("salmo")) {
                    // El número no es un atributo sino un tag hijo
                } else if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("numero")) {
                    parser.next();
                    if (parser.getText().equals(String.valueOf(numero))) {
                        salmoEncontrado = true;
                    }
                } else if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("texto") && salmoEncontrado) {
                    parser.next();
                    textoSalmo = parser.getText();
                    break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer salmos.xml: " + e.getMessage());
            textoSalmo = "El Señor es mi pastor, nada me falta.";
            autorSalmo = "Salmo 23";
        }
        return new String[]{textoSalmo, autorSalmo};
    }


    private void configurarNavegacion() {
        btnBiblia.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BibliaActivity.class));
        });

        btnRosario.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RosarioActivity.class));
        });

        btnDiario.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DiarioActivity.class));
        });
    }

    private void configurarFechaYSanto() {
        LocalDate hoy = LocalDate.now();
        Locale localeEs = new Locale("es", "ES");

        String diaSemana = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, localeEs);
        String mesNombre = hoy.getMonth().getDisplayName(TextStyle.FULL, localeEs);
        String diaSemanaCapitalized = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);

        String fechaFormateada = diaSemanaCapitalized +
                ", " + hoy.getDayOfMonth() + " de " + mesNombre;

        fechaTextView.setText(fechaFormateada);

        obtenerSantoDelDiaAsync(hoy.getDayOfMonth(), hoy.getMonthValue());
    }

    private void obtenerSantoDelDiaAsync(int dia, int mes) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String santo = obtenerSantoDelDiaSincrono(dia, mes);
            handler.post(() -> {
                santoDelDiaTextView.setText(santo);
            });
        });
    }

    private String obtenerSantoDelDiaSincrono(int dia, int mes) {
        String santo = "Buscando Santo...";
        try (XmlResourceParser parser = getResources().getXml(R.xml.santos)) {
            int eventType = parser.getEventType();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("dia")) {
                    int mesAtributo = parseAttributeInt(parser.getAttributeValue(null, "mes"));
                    int diaAtributo = parseAttributeInt(parser.getAttributeValue(null, "numero"));
                    if (mes == mesAtributo && dia == diaAtributo) {
                        santo = parser.getAttributeValue(null, "santo");
                        break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer santos.xml: " + e.getMessage());
            santo = "Paz y Bien";
        }
        return santo;
    }

    private int parseAttributeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}