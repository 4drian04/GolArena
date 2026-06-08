package com.adriangg.golarena;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.adriangg.golarena.Entity.Jugadores;
import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    public static void loadCsvIfNeeded(Context context, AppDatabase db, Runnable onFinished) {

        new Thread(() -> {

            SharedPreferences prefs =
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

            String loadedVersion = prefs.getString("csv_version", null);
            String currentVersion = BuildConfig.CSV_VERSION;

            if (!currentVersion.equals(loadedVersion)) {

                try {

                    CSVReader reader = new CSVReader(
                            new InputStreamReader(context.getAssets().open("EAFC26.csv"))
                    );

                    List<String[]> rows = reader.readAll();
                    List<Jugadores> players = new ArrayList<>();

                    // Saltamos header (posición 0)
                    for (int i = 1; i < rows.size(); i++) {

                        String[] tokens = rows.get(i);

                        Jugadores player = new Jugadores(
                                tokens[2],                     // Name
                                tokens[tokens.length - 1],      // Image URL
                                Integer.parseInt(tokens[47]),  // Age
                                tokens[48],                    // Nation
                                Integer.parseInt(tokens[4])   // OVR
                        );

                        players.add(player);
                    }

                    db.runInTransaction(() -> {
                        db.daoJugadores().deleteAll();

                        // Insertar en bloques de 500
                        int batchSize = 500;
                        for (int i = 0; i < players.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, players.size());
                            db.daoJugadores().insertAll(players.subList(i, end));
                        }
                    });

                    prefs.edit().putString("csv_version", currentVersion).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Notificar al hilo principal cuando termine
            if (onFinished != null) {
                new Handler(Looper.getMainLooper()).post(onFinished);
            }

        }).start();
    }
}