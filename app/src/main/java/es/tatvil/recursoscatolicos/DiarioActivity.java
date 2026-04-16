package es.tatvil.recursoscatolicos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DiarioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "DiarioPrefs";
    private static final String KEY_PREFIX = "reflexion_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        recyclerView = findViewById(R.id.recyclerViewDiario);
        fabAdd = findViewById(R.id.fabAdd);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DiarioActivity.this, DiarioNewItemActivity.class);
            startActivity(intent);
        });

        actualizarLista();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarLista();
    }

    private void actualizarLista() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        
        // Usamos un TreeMap para que las entradas salgan ordenadas por fecha (clave)
        // Por defecto, TreeMap ordena de forma ascendente.
        // Si queremos que la más reciente aparezca primero, podemos usar el comparador inverso.
        TreeMap<String, String> sortedEntries = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
        
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(KEY_PREFIX)) {
                sortedEntries.put(entry.getKey(), entry.getValue().toString());
            }
        }

        List<DiarioAdapter.DiarioEntry> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : sortedEntries.entrySet()) {
            // Convertimos la clave reflexion_YYYYMMDD a un formato legible YYYY-MM-DD
            String rawDate = entry.getKey().replace(KEY_PREFIX, "");
            String formattedDate = rawDate.substring(0, 4) + "-" + 
                                  rawDate.substring(4, 6) + "-" + 
                                  rawDate.substring(6, 8);
            
            items.add(new DiarioAdapter.DiarioEntry(formattedDate, entry.getValue()));
        }

        DiarioAdapter adapter = new DiarioAdapter(items);
        recyclerView.setAdapter(adapter);
    }
}
