package es.tatvil.recursoscatolicos.model;

public class OracionDia {
    private String titulo;
    private String hora;
    private String descripcionDetallada;
    private boolean esExpandido; // Estado para saber si la tarjeta está abierta

    // Constructor
    public OracionDia(String titulo, String hora, String descripcionDetallada) {
        this.titulo = titulo;
        this.hora = hora;
        this.descripcionDetallada = descripcionDetallada;
        this.esExpandido = false; // Por defecto, siempre colapsado
    }

    // Getters y Setters...
    public String getTitulo() { return titulo; }
    public String getHora() { return hora; }
    public String getDescripcionDetallada() { return descripcionDetallada; }
    public boolean isExpandido() { return esExpandido; }
    public void setExpandido(boolean expandido) { esExpandido = expandido; }
}
