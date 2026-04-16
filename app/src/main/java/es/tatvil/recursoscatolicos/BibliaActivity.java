package es.tatvil.recursoscatolicos;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibliaActivity extends AppCompatActivity {

    private TextView textView;
    private Button buttonNext;
    private Spinner spinnerBooks;
    private Spinner spinnerChapters;

    private Map<String, Map<Integer, Integer>> bibleData = new HashMap<>();

    private String currentBook = "";
    private int currentChapter = 0;
    private int currentVerseIndex = 0;
    private String[] currentVerses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblia);

        textView = findViewById(R.id.text_view);
        buttonNext = findViewById(R.id.buttonSiguienteVersiculo);
        spinnerBooks = findViewById(R.id.spinner_books);
        spinnerChapters = findViewById(R.id.spinner_chapters);

        // Initialize bibleData
        initializeBibleData();

        List<String> bookNames = new ArrayList<>(bibleData.keySet());
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bookNames);
        spinnerBooks.setAdapter(bookAdapter);

        spinnerBooks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentBook = bookNames.get(position);
                Map<Integer, Integer> chapters = bibleData.get(currentBook);
                List<Integer> chapterList = new ArrayList<>(chapters.keySet());
                ArrayAdapter<Integer> chapterAdapter = new ArrayAdapter<>(BibliaActivity.this, android.R.layout.simple_spinner_item, chapterList);
                spinnerChapters.setAdapter(chapterAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerChapters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentChapter = (int) parent.getItemAtPosition(position);
                currentVerses = getResources().getStringArray(bibleData.get(currentBook).get(currentChapter));
                currentVerseIndex = 0;
                updateVerse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentVerseIndex = (currentVerseIndex + 1) % currentVerses.length;
                updateVerse();
            }
        });
    }

    private void initializeBibleData() {
        bibleData.put("San Juan", new HashMap<Integer, Integer>() {{
            put(1, R.array.san_juan_chapter_1);
            put(3, R.array.san_juan_chapter_3);
        }});
        bibleData.put("San Lucas", new HashMap<Integer, Integer>() {{
            put(1, R.array.san_lucas_chapter_1);
            put(2, R.array.san_lucas_chapter_2);
        }});
        bibleData.put("Génesis", new HashMap<Integer, Integer>() {{
            put(1, R.array.genesis_chapter_1);
        }});
        bibleData.put("Salmos", new HashMap<Integer, Integer>() {{
            put(23, R.array.salmos_chapter_1);
            put(91, R.array.salmos_chapter_2);
        }});
    }

    private void updateVerse() {
        textView.setText(currentVerses[currentVerseIndex]);
    }
}