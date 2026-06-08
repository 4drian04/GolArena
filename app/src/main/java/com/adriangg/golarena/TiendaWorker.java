package com.adriangg.golarena;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adriangg.golarena.Entity.JugadorTienda;
import com.adriangg.golarena.Entity.Jugadores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hacemos un worker para que la tienda se actualice cada 24 horas
 */
public class TiendaWorker extends Worker {

    public TiendaWorker(@NonNull Context context,
                        @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            AppDatabase db = DatabaseProvider.getDatabase(getApplicationContext());
            // Obtener todos los jugadores de Room
            List<Jugadores> todosJugadores = db.daoJugadores().getJugadores();

            if (todosJugadores.isEmpty()) {
                return Result.retry();
            }

            List<Jugadores> filtrados = new ArrayList<>();
            for (Jugadores j : todosJugadores) {
                if (j.media > 74) {
                    filtrados.add(j);
                }
            }

            // Mezclar y tomar 10 aleatorios
            Collections.shuffle(filtrados);
            List<Jugadores> seleccionados = filtrados.subList(0, Math.min(10, filtrados.size()));

            // Convertir a JugadorTienda
            List<JugadorTienda> listaJugadores = new ArrayList<>();
            for (Jugadores p : seleccionados) {
                listaJugadores.add(new JugadorTienda(
                        p.nombre,
                        p.urlFoto,
                        p.edad,
                        p.nacionalidad,
                        p.media
                ));
            }

            // Guardar en tabla de tienda
            guardarJugadores(db, listaJugadores);
            return Result.success();

        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void guardarJugadores(AppDatabase db, List<JugadorTienda> listaJugadores) {
        // Borra los antiguos y guarda los nuevos
        db.daoTienda().deleteJugador();
        db.daoTienda().insertAll(listaJugadores);
    }
}
