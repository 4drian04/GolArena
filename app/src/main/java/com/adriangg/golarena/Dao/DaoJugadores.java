package com.adriangg.golarena.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.adriangg.golarena.Entity.Jugadores;

import java.util.List;

@Dao
public interface DaoJugadores {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Jugadores> players);

    @Query("SELECT * FROM Jugadores")
    List<Jugadores> getJugadores();

    @Query("DELETE FROM Jugadores")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM Jugadores")
    int countPlayers();
}
