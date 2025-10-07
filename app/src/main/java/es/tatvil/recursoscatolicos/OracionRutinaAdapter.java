package es.tatvil.recursoscatolicos;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater; // Necesitas este import
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import es.tatvil.recursoscatolicos.model.OracionDia;

public class OracionRutinaAdapter extends RecyclerView.Adapter<OracionRutinaAdapter.ViewHolder> {

    private final List<OracionDia> listaOraciones;

    public OracionRutinaAdapter(List<OracionDia> listaOraciones) {
        this.listaOraciones = listaOraciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oracion_card, parent, false); // Asegúrate de que item_oracion_card es el nombre de tu layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OracionDia oracion = listaOraciones.get(position);

        holder.tituloTextView.setText(oracion.getHora() + ": " + oracion.getTitulo());
        holder.contenidoTextView.setText(oracion.getDescripcionDetallada());

        // **LÓGICA DE EXPANSIÓN/COLAPSO**
        boolean isExpanded = oracion.isExpandido();
        holder.contenidoLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // **IMPORTANTE: Usa tus nombres de drawable corregidos (ej. ic_expand_up/down)**
        // Yo usaré los nombres corregidos que te sugerí en la respuesta anterior:
        holder.expandIcon.setImageResource(isExpanded ? R.drawable.ic_flecha_arriba : R.drawable.ic_flecha_abajo);

        // Lógica para mostrar/ocultar el botón "Ir al Rosario Completo"
        if (oracion.getTitulo().contains("Rosario")) {
            holder.rosarioButton.setVisibility(View.VISIBLE);
            holder.rosarioButton.setOnClickListener(v -> {
                // Aquí puedes iniciar la actividad del Rosario
                holder.rosarioButton.getContext().startActivity(new Intent(holder.rosarioButton.getContext(), RosarioActivity.class));
            });
        } else {
            holder.rosarioButton.setVisibility(View.GONE);
        }

        // Listener de click en la tarjeta para cambiar el estado
        holder.headerClickable.setOnClickListener(v -> {
            oracion.setExpandido(!isExpanded); // Cambia el estado
            notifyItemChanged(position); // Refresca solo este elemento
        });
    }

    @Override
    public int getItemCount() {
        return listaOraciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ... (Tu código de ViewHolder se mantiene igual, ya que está correcto) ...
        TextView tituloTextView;
        TextView contenidoTextView;
        LinearLayout contenidoLayout;
        ConstraintLayout headerClickable;
        ImageView expandIcon;
        Button rosarioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headerClickable = itemView.findViewById(R.id.header_card_clickable);
            tituloTextView = itemView.findViewById(R.id.card_titulo_seccion);
            contenidoLayout = itemView.findViewById(R.id.card_contenido_expandible);
            contenidoTextView = itemView.findViewById(R.id.card_contenido_detallado);
            expandIcon = itemView.findViewById(R.id.card_icono_expandir);
            rosarioButton = itemView.findViewById(R.id.btn_ir_a_rosario);
        }
    }
}