package es.tatvil.recursoscatolicos;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
        btnBiblia = findViewById(R.id.btn_biblia_card);
        btnRosario = findViewById(R.id.btn_rosario_card);
        btnDiario = findViewById(R.id.journal_card);
        quoteTextView = findViewById(R.id.quote_text);
        quoteAuthorTextView = findViewById(R.id.quote_author);

        configurarSalmoDelDia();
        // configurarEvangelioDelDia();

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


/*
    private void configurarEvangelioDelDia() {
        LocalDate hoy = LocalDate.now();
        obtenerEvangelioAsync(hoy.getDayOfMonth(), hoy.getMonthValue());
    }

    private void obtenerEvangelioAsync(int dia, int mes) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String[] resultado = obtenerEvangelioSincrono(dia, mes);
            handler.post(() -> {
                gospelSnippetTextView.setText(resultado[0]);
                if (resultado[1] != null && !resultado[1].isEmpty()) {
                    labelLecturaTextView.setText(getString(R.string.evangelio_hoy) + " (" + resultado[1] + ")");
                }
            });
        });
    }

    private String[] obtenerEvangelioSincrono(int dia, int mes) {
        String texto = "Referencia no encontrada en la Biblia offline.";
        String cita = "";
        int libroId = -1, capNum = -1;
        String vRange = "";

        // 1. Buscar la referencia en evangelios.xml
        try (XmlResourceParser parser = getResources().getXml(R.xml.evangelios)) {
            int eventType = parser.getEventType();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("dia")) {
                    int m = parseAttributeInt(parser.getAttributeValue(null, "mes"));
                    int d = parseAttributeInt(parser.getAttributeValue(null, "numero"));
                    if (m == mes && d == dia) {
                        cita = parser.getAttributeValue(null, "cita");
                        libroId = parseAttributeInt(parser.getAttributeValue(null, "libro"));
                        capNum = parseAttributeInt(parser.getAttributeValue(null, "capitulo"));
                        vRange = parser.getAttributeValue(null, "versiculos");
                        break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer evangelios.xml: " + e.getMessage());
        }

        // 2. Si tenemos la referencia, buscar el texto en biblia.xml
        if (libroId != -1 && capNum != -1) {
            String textoBiblia = buscarTextoEnBiblia(libroId, capNum, vRange);
            if (textoBiblia != null) texto = textoBiblia;
        }

        return new String[]{texto, cita};
    }

    private String buscarTextoEnBiblia(int libroId, int capNum, String vRange) {
        StringBuilder sb = new StringBuilder();
        // Intentamos cargar el archivo específico del libro (ej: libro1.xml)
        int resId = getResources().getIdentifier("libro" + libroId, "xml", getPackageName());

        // Si no existe el archivo individual, intentamos con el archivo general
        if (resId == 0) {
            resId = R.xml.biblia;
        }

        try (XmlResourceParser parser = getResources().getXml(resId)) {
            int eventType = parser.getEventType();
            boolean libroEncontrado = false;
            boolean capituloEncontrado = false;

            // Si el archivo es individual (ej: libro1.xml), el tag raíz es <book> o <bible>
            // Ajustamos la lógica para que funcione en ambos casos
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String name = parser.getName();
                    if (name.equals("book")) {
                        // Si es el archivo general, comprobamos el número.
                        // Si es el archivo individual, asumimos que es el correcto o comprobamos igual.
                        String numAttr = parser.getAttributeValue(null, "number");
                        if (numAttr == null || parseAttributeInt(numAttr) == libroId) {
                            libroEncontrado = true;
                        }
                    } else if (libroEncontrado && name.equals("chapter")) {
                        if (parseAttributeInt(parser.getAttributeValue(null, "number")) == capNum) {
                            capituloEncontrado = true;
                        }
                    } else if (capituloEncontrado && name.equals("verse")) {
                        int vNum = parseAttributeInt(parser.getAttributeValue(null, "number"));
                        if (estaEnRango(vNum, vRange)) {
                            parser.next();
                            if (parser.getEventType() == XmlResourceParser.TEXT) {
                                sb.append(parser.getText()).append(" ");
                            }
                        }
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    String name = parser.getName();
                    if (name.equals("book") && libroEncontrado) {
                        // Si ya terminamos el libro en el archivo general, salimos
                        if (resId == R.xml.biblia) libroEncontrado = false;
                    }
                    if (name.equals("chapter") && capituloEncontrado) {
                        if (sb.length() > 0) return sb.toString().trim();
                        capituloEncontrado = false;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer biblia: " + e.getMessage());
        }
        return sb.length() > 0 ? sb.toString().trim() : null;
    }

    private boolean estaEnRango(int vNum, String range) {
        if (range == null || range.isEmpty()) return true;
        try {
            if (range.contains("-")) {
                String[] parts = range.split("-");
                int inicio = Integer.parseInt(parts[0]);
                int fin = Integer.parseInt(parts[1]);
                return vNum >= inicio && vNum <= fin;
            } else if (range.contains(",")) {
                String[] parts = range.split(",");
                for (String p : parts) {
                    if (Integer.parseInt(p.trim()) == vNum) return true;
                }
                return false;
            } else {
                return vNum == Integer.parseInt(range);
            }
        } catch (Exception e) {
            return true;
        }
    }
*/

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

    private int parseAttributeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}