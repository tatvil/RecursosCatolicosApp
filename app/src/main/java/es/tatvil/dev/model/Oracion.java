package es.tatvil.dev.model;


public class Oracion {
    private String texto;
    private int audioResId;
    private boolean esAveMaria; // Para controlar las cuentas visuales
    private String tipoOracion; // Ej: "INTRO_PADRE", "AVEMARIA", "MEDITACION", "GLORIA"

    public Oracion(String texto, int audioResId, boolean esAveMaria, String tipoOracion) {
        this.texto = texto;
        this.audioResId = audioResId;
        this.esAveMaria = esAveMaria;
        this.tipoOracion = tipoOracion;
    }

    // Getters
    public String getTexto() {
        return texto;
    }

    public int getAudioResId() {
        return audioResId;
    }

    public boolean esAveMaria() {
        return esAveMaria;
    }

    public String getTipoOracion() {
        return tipoOracion;
    }
}

