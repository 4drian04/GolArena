package com.adriangg.golarena;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.adriangg.golarena.Entity.Sobre;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    protected static String sharedPrefFile =
            "com.example.gol_arena";

    public static final String urlNoticiasEspanha = "https://site.api.espn.com/apis/site/v2/sports/soccer/esp.1/news?lang=es";
    public static SharedPreferences sharedPreferences;
    public static AppDatabase appDatabase;

    LinearLayout linearLayoutProgressBar;
    FrameLayout frameLayout;

    public static final int K = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        linearLayoutProgressBar = findViewById(R.id.menuProgressBarMainLayout);
        frameLayout = findViewById(R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameLayout.setVisibility(View.GONE);
        linearLayoutProgressBar.setVisibility(View.VISIBLE);
        //Cargamos el CSV del CSV la primera vez que el usuario entra a la aplicación
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "golArenaDb").build();
        DatabaseInitializer.loadCsvIfNeeded(this, appDatabase, () -> runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                linearLayoutProgressBar.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                bottomNavigationView.setEnabled(true);
                loadFragment(new NoticiasFragment());

                // Programar Worker de la tienda después de cargar CSV
                PeriodicWorkRequest tiendaRequest = new PeriodicWorkRequest.Builder(
                        TiendaWorker.class,
                        1, TimeUnit.DAYS
                ).build();

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                        "TiendaDiaria",
                        ExistingPeriodicWorkPolicy.KEEP,
                        tiendaRequest
                );
            }
        }));
        //Configuramos el sharedPreferences
        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        //Configuramos el bottomNavigation para poder navegar sobre las distintas pestañas
        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_news) {
                selectedFragment = new NoticiasFragment();

            } else if (id == R.id.nav_quiz) {
                selectedFragment = new QuizFragment();

            } else if (id == R.id.nav_sobres) {
                selectedFragment = new JugadoresFragment();

            } else if (id == R.id.nav_store) {
                selectedFragment = new StoreFragment();

            } else if (id == R.id.nav_chatbot) {
                selectedFragment = new ChatBotFragment();
            }
            if (selectedFragment != null && !isDestroyed()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commitAllowingStateLoss(); // evita crashes por estado
            }
            return true;
        });
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}