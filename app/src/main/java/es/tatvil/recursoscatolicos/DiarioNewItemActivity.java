package es.tatvil.recursoscatolicos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiarioNewItemActivity extends AppCompatActivity {

    private EditText etReflexion;
    private FloatingActionButton fabSave;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "DiarioPrefs";
    private static final String KEY_PREFIX = "reflexion_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario_new_item);

        etReflexion = findViewById(R.id.et_reflexion);
        fabSave = findViewById(R.id.fab_save);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Cargar reflexión del día si existe
        String todayKey = getTodayKey();
        String savedText = sharedPreferences.getString(todayKey, "");
        etReflexion.setText(savedText);

        fabSave.setOnClickListener(v -> {
            String texto = etReflexion.getText().toString();
            if (!texto.isEmpty()) {
                sharedPreferences.edit().putString(todayKey, texto).apply();
                Toast.makeText(this, "Reflexión guardada en tu corazón", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Escribe algo para el Señor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTodayKey() {
        return KEY_PREFIX + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    }
}