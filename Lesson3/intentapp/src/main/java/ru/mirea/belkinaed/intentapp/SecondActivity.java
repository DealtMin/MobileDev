package ru.mirea.belkinaed.intentapp;

import android.app.appsearch.GetByDocumentIdRequest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        text = findViewById(R.id.textView2);
        Intent intent = getIntent();
        String string = intent.getStringExtra("date");
        Integer number = intent.getIntExtra("number", 0);
        String fullstring = String.format("КВАДРАТ ЗНАЧЕНИЯ МОЕГО НОМЕРА " +
                "ПО СПИСКУ В ГРУППЕ СОСТАВЛЯЕТ %s, а текущее время " +
                string, number*number);

        text.setText(fullstring);
    }
}