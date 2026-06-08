package com.adriangg.golarena.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.adriangg.golarena.Entity.Jugador;

import java.util.List;

@Dao
public interface DaoJugador {


    @Query("SELECT * FROM Jugador")
    List<Jugador> getJugadores();
    @Insert
    void insertarJugador(Jugador...jugador);

    @Delete
    int deleteJugador(Jugador jugador);

    @Query("SELECT * FROM Jugador WHERE nombre LIKE '%' || :nombreJugador || '%' COLLATE NOCASE")
    List<Jugador> getJugadoresPorNombre(String nombreJugador);
}
