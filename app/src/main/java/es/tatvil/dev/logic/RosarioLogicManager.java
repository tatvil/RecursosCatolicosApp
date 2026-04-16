package es.tatvil.dev.logic;

import android.util.Log;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import es.tatvil.dev.data.RosarioDataProvider;
import es.tatvil.dev.model.Misterio;
import es.tatvil.dev.model.Oracion;
import es.tatvil.dev.R; // Asegúrate de que esta importación sea correcta para acceder a R.drawable

public class RosarioLogicManager {

    public RosarioDataProvider dataProvider;
    private List<Misterio> misteriosDelDia;

    // Variables de estado para el progreso del rosario
    // etapaRosario: 0=Intro, 1-5=Misterio (decena), 6=Conclusión
    private int etapaRosario = 0;
    // oracionEnEtapaIndex: Índice de la oración dentro de la etapa actual
    private int oracionEnEtapaIndex = 0;

    public static final int NUM_AVEMARIAS_DECADA = 10;
    // Índice del primer Ave María en la plantilla de la década.
    // Esto es útil para saber cuántas Ave Marías se han rezado.
    public static final int INDEX_PRIMER_AVEMARIA_TEMPLATE = 2;

    public RosarioLogicManager(RosarioDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        // Obtenemos los misterios del día al inicializar la clase.
        this.misteriosDelDia = dataProvider.getMisteriosDelDia();
        Log.d("RosarioLogicManager","Misterio del dia: " + this.misteriosDelDia);
    }

    /**
     * Resetea el progreso del rosario al inicio.
     */
    public void resetRosario() {
        etapaRosario = 0;
        Log.d("RosarioLogicManager","Rest Rosario - EtapaRosario: " + etapaRosario);
        oracionEnEtapaIndex = 0;
        // Volvemos a obtener los misterios del día por si el día ha cambiado.
        this.misteriosDelDia = dataProvider.getMisteriosDelDia();
    }

    /**
     * Avanza a la siguiente oración del Rosario.
     *
     * @return El objeto Oracion actual, o null si el rosario ha terminado.
     */
    // Inside RosarioLogicManager.java

    public Oracion getNextOracion() {
        // 1. Check if the current prayer index is the last one in the current stage.
        // If it is, advance the stage.
        Log.d("RosarioLogicManager","getNextOracion - EtapaRosario: " + etapaRosario);
        if (isCurrentStageComplete()) {
            etapaRosario++;
            oracionEnEtapaIndex = 0; // Start the new stage at index 0
        } else {
            // 2. If the current stage is NOT complete, just advance the index.
            oracionEnEtapaIndex++;
        }

        // 3. Get the prayer corresponding to the new (or incremented) state.
        // This is now the ONLY way to get the prayer.
        Oracion nextOracion = getCurrentOracion();

        // 4. If the new state (e.g., etapa > 6) results in null, the rosary is truly over.
        return nextOracion;
    }

    // *** NEW HELPER METHOD ***
    private boolean isCurrentStageComplete() {
        int totalOracionesInStage = -1; // -1 to be safe
        Log.d("RosarioLogicManager","isCurrentStageCompleted - EtapaRosario: " + etapaRosario);
        if (etapaRosario == 0) { // Introducción
            totalOracionesInStage = dataProvider.getIntroOraciones().size();
        } else if (etapaRosario >= 1 && etapaRosario <= 5) { // Décadas (Mystery)
            // DecadaTemplate + 1 (for the Meditation at index 0)
            totalOracionesInStage = dataProvider.getDecadaTemplateOraciones().size() + 1;
        } else if (etapaRosario == 6) { // Conclusión
            totalOracionesInStage = dataProvider.getConclusionOraciones().size();
        } else {
            // Already completed
            return true;
        }

        // The stage is complete if we have just finished the last prayer (at index = totalOracionesInStage - 1).
        // The index for the *next* operation should be totalOracionesInStage.
        return oracionEnEtapaIndex >= totalOracionesInStage;
    }
    /**
     * Devuelve la oración actual basada en el estado actual (etapa y índice).
     * Este método es nuevo y centraliza la lógica de obtener la oración.
     *
     * @return El objeto Oracion actual, o null si el rosario ha terminado o la etapa es inválida.
     */
    public Oracion getCurrentOracion() {
        if (etapaRosario == 0) { // Introducción
            List<Oracion> introOraciones = dataProvider.getIntroOraciones();
            if (oracionEnEtapaIndex < introOraciones.size()) {
                return introOraciones.get(oracionEnEtapaIndex);
            }
            Log.d("RosarioLogicManager","EtapaRosario: " + etapaRosario);
        } else if (etapaRosario >= 1 && etapaRosario <= 5) { // Décadas
            // La meditación ya no se trata como una etapa separada.
            // La lógica ahora toma la meditación del misterio actual
            // y la combina con la plantilla de la década.
            Misterio misterioActual = misteriosDelDia.get(etapaRosario);
            List<Oracion> decadaTemplate = dataProvider.getDecadaTemplateOraciones();

            // Meditación es la primera "oración" de la década (índice 0)
            if (oracionEnEtapaIndex == 0) {
                return misterioActual.getMeditation();
            }
            // El resto de las oraciones vienen de la plantilla de la década.
            // Hay que ajustar el índice porque la meditación ocupa el índice 0.
            if (oracionEnEtapaIndex - 1 < decadaTemplate.size()) {
                return decadaTemplate.get(oracionEnEtapaIndex - 1);
            }
        } else if (etapaRosario == 6) { // Conclusión
            List<Oracion> conclusionOraciones = dataProvider.getConclusionOraciones();
            if (oracionEnEtapaIndex < conclusionOraciones.size()) {
                return conclusionOraciones.get(oracionEnEtapaIndex);
            }
        }
        return null; // El rosario ha terminado o la etapa es inválida
    }


    // Métodos getter para el estado actual
    public int getEtapaRosario() {
        return etapaRosario;
    }

    public int getOracionEnEtapaIndex() {
        return oracionEnEtapaIndex;
    }

    public List<Misterio> getMisteriosDelDia() {
        return misteriosDelDia;
    }

    public boolean isRosarioCompleted() {
        // Ahora el rosario se considera completo cuando la etapa es mayor que 6
        // y el índice de la oración en la etapa de conclusión ha terminado.
        // La condición en getNextOracion() y getCurrentOracion() maneja el resto.
        return etapaRosario > 6;
    }

    public String getNombreMisterioActual() {
        if (etapaRosario >= 1 && etapaRosario <= 5) {
            return misteriosDelDia.get(etapaRosario - 1).getNombre();
        }
        return ""; // O algún valor predeterminado
    }

    public int getImagenMisterioActualResId() {
        if (etapaRosario >= 1 && etapaRosario <= 5) {
            return misteriosDelDia.get(etapaRosario - 1).getImagenResId();
        }
        // Si no estamos en una década, devolvemos una imagen por defecto
        return R.drawable.rosario_general;
    }

    public int getAveMariasRezadaCount() {
        // Solo contamos las Ave Marías si estamos en una década y si la oración actual es un Ave María.
        if (etapaRosario >= 1 && etapaRosario <= 5) {
            // Se asume que la plantilla de la década tiene el Ave María en los índices
            // siguientes a la meditación y el Padre Nuestro.
            // Si el índice de la oración actual es mayor que el índice del primer Ave María de la plantilla,
            // podemos calcular cuántas se han rezado.
            if (oracionEnEtapaIndex >= (1 + dataProvider.getDecadaTemplateOraciones().indexOf(dataProvider.getDecadaTemplateOraciones().get(INDEX_PRIMER_AVEMARIA_TEMPLATE)))) {
                return oracionEnEtapaIndex - (1 + dataProvider.getDecadaTemplateOraciones().indexOf(dataProvider.getDecadaTemplateOraciones().get(INDEX_PRIMER_AVEMARIA_TEMPLATE)));
            }
        }
        return 0; // No estamos en una Ave María
    }


    public String getTipoMisterioDelDia() {
        DayOfWeek diaActual = LocalDate.now().getDayOfWeek();
        if (diaActual == DayOfWeek.MONDAY || diaActual == DayOfWeek.SATURDAY) {
            return "Misterios Gozosos";
        } else if (diaActual == DayOfWeek.TUESDAY || diaActual == DayOfWeek.FRIDAY) {
            return "Misterios Dolorosos";
        } else if (diaActual == DayOfWeek.WEDNESDAY || diaActual == DayOfWeek.SUNDAY) {
            return "Misterios Gloriosos";
        } else if (diaActual == DayOfWeek.THURSDAY) {
            return "Misterios Luminosos";
        }
        return "Misterios del Día";
    }

}


