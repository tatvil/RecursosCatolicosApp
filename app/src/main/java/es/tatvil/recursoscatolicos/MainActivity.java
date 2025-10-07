package es.tatvil.recursoscatolicos;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.tatvil.recursoscatolicos.data.RutinaDataManager;
import es.tatvil.recursoscatolicos.model.OracionDia;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView fechaTextView;
    private TextView santoDelDiaTextView;
    private RecyclerView recyclerView;
    private Button btnBiblia;
    private Button btnRosario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicialización de Vistas
        fechaTextView = findViewById(R.id.fecha);
        santoDelDiaTextView = findViewById(R.id.santodeldia);
        recyclerView = findViewById(R.id.recycler_view_rutina);
        btnBiblia = findViewById(R.id.btn_biblia);
        btnRosario = findViewById(R.id.btn_rosario);

        // 2. Configuración de la Cabecera
        configurarFechaYSanto();

        // 3. Configuración del Cuerpo (Rutina)
        configurarRutinaOracion();

        // 4. Configuración de Listeners para el Pie de Página
        configurarNavegacion();
    }

    // -----------------------------------------------------------------
    //  MÉTODOS DE LÓGICA
    // -----------------------------------------------------------------

    /**
     * Configura el RecyclerView. Delega la obtención de datos al RutinaDataManager.
     */
    private void configurarRutinaOracion() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // **USO CORRECTO: Inicializa el DataManager y obtiene los datos del XML**
        RutinaDataManager dataManager = new RutinaDataManager(this);
        List<OracionDia> listaOraciones = dataManager.prepararDatosRutina();

        Log.d(TAG, "Tamaño de la lista de oraciones: " + listaOraciones.size());

        OracionRutinaAdapter adapter = new OracionRutinaAdapter(listaOraciones);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Configura los OnClickListeners para los botones de navegación.
     */
    private void configurarNavegacion() {
        btnBiblia.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BibliaActivity.class));
        });

        btnRosario.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RosarioActivity.class));
        });
    }

    /**
     * Configura la fecha actual y obtiene el Santo del día de forma asíncrona.
     */
    private void configurarFechaYSanto() {
        // Formato de Fecha
        LocalDate hoy = LocalDate.now();
        Locale localeEs = new Locale("es", "ES");

        // Formato: "Domingo, 4 de Octubre de 2025"
        String diaSemana = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, localeEs);
        String mesNombre = hoy.getMonth().getDisplayName(TextStyle.FULL, localeEs);

        // Primera letra en mayúscula (ej: "domingo" -> "Domingo")
        String diaSemanaCapitalized = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);

        String fechaFormateada = diaSemanaCapitalized +
                ", " + hoy.getDayOfMonth() + " de " + mesNombre + " de " + hoy.getYear();

        fechaTextView.setText(fechaFormateada);

        // Carga Asíncrona del Santo
        obtenerSantoDelDiaAsync(hoy.getDayOfMonth(), hoy.getMonthValue());
    }

    /**
     * Obtiene el santo del día de forma asíncrona (ExecutorService).
     */
    private void obtenerSantoDelDiaAsync(int dia, int mes) {
        // Hilo de fondo para la tarea pesada (parsing de XML)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Handler para volver al hilo principal (UI)
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Tarea en el HILO DE FONDO
            String santo = obtenerSantoDelDiaSincrono(dia, mes);

            // Actualización de la UI en el HILO PRINCIPAL
            handler.post(() -> {
                santoDelDiaTextView.setText(santo);
            });
        });
    }

    /**
     * Lógica SÍNCRONA de lectura del XML de santos. (Debe ejecutarse en un hilo de fondo).
     */
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
                        break; // Santo encontrado, salir del bucle
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer santos.xml: " + e.getMessage());
            santo = "Error al cargar el Santo";
        }

        if (santo.equals("Buscando Santo...")) {
            santo = "No hay Santo registrado para hoy";
        }

        return santo;
    }

    /**
     * Método auxiliar para convertir un String de atributo XML a int.
     */
    private int parseAttributeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Formato numérico incorrecto en el XML de santos: " + value);
            return -1; // Indica un valor inválido
        }
    }
}