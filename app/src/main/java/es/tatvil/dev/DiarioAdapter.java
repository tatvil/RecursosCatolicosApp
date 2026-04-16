package es.tatvil.dev;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DiarioAdapter extends RecyclerView.Adapter<DiarioAdapter.DiarioViewHolder> {

    private List<DiarioEntry> entries;

    public DiarioAdapter(List<DiarioEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public DiarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diario, parent, false);
        return new DiarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiarioViewHolder holder, int position) {
        DiarioEntry entry = entries.get(position);
        holder.tvDate.setText(entry.getDate());
        holder.tvText.setText(entry.getText());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class DiarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvText;

        public DiarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvText = itemView.findViewById(R.id.tv_item_text);
        }
    }

    public static class DiarioEntry {
        private String date;
        private String text;

        public DiarioEntry(String date, String text) {
            this.date = date;
            this.text = text;
        }

        public String getDate() { return date; }
        public String getText() { return text; }
    }
}
