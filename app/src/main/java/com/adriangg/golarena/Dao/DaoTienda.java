package com.adriangg.golarena.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.adriangg.golarena.Entity.JugadorTienda;

import java.util.List;

@Dao
public interface DaoTienda {

    @Query("SELECT * FROM JugadorTienda")
    LiveData<List<JugadorTienda>> getJugadores();
    @Insert
    void insertAll(List<JugadorTienda> jugadores);

    @Query("DELETE FROM JugadorTienda")
    int deleteJugador();

    @Query("DELETE FROM JugadorTienda WHERE nombre==:nombreJugador")
    int deleteJugadorComprado(String nombreJugador);
}
