package es.tatvil.recursoscatolicos.data;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.List;

import es.tatvil.recursoscatolicos.R;
import es.tatvil.recursoscatolicos.model.OracionDia;

public class RutinaDataManager {

    private static final String TAG = "RutinaDataManager";
    private final Context context;

    // Constructor que recibe el Context para acceder a los recursos (R.xml, R.string)
    public RutinaDataManager(Context context) {
        this.context = context;
    }

    /**
     * Prepara la lista de oraciones leyendo la estructura desde rutinas.xml
     * y componiendo el contenido detallado a partir de strings.xml.
     */
    public List<OracionDia> prepararDatosRutina() {
        List<OracionDia> data = new ArrayList<>();
        Resources res = context.getResources();

        // 1. Cadenas base para componer el contenido detallado
        // Asegúrate de que R.string.angelus y R.string.bendicion_alimentos existan en strings.xml
        String angelus = res.getString(R.string.angelus);
        String bendicionAlimentos = res.getString(R.string.bendicion_alimentos);

        // 2. Abrir el archivo rutinas.xml
        XmlResourceParser parser = res.getXml(R.xml.rutinas);

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("momento")) {

                    // Leer los atributos
                    // Usamos getAttributeValue para leer strings simples
                    String titulo = parser.getAttributeValue(null, "titulo");
                    String hora = parser.getAttributeValue(null, "hora");

                    // *** LA CORRECCIÓN CLAVE: Leer la referencia (@string/...) directamente como ID entero ***
                    int contentId = parser.getAttributeResourceValue(null, "contenido_ref", 0);

                    // Log para depuración
                    Log.d(TAG, "Título: " + titulo + ", Hora: " + hora + ", Contenido ID: " + contentId);


                    if (titulo != null && contentId != 0) {
                        // Componer el contenido detallado. Si los strings de contenido usan %1$s y %2$s,
                        // serán reemplazados por angelus y bendicionAlimentos respectivamente.
                        String contenidoDetallado = res.getString(contentId, angelus, bendicionAlimentos);

                        data.add(new OracionDia(titulo, hora, contenidoDetallado));
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear rutinas.xml o strings.xml", e);
        } finally {
            // Es buena práctica cerrar el parser
            parser.close();
        }

        return data;
    }
}