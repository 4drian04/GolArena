package com.adriangg.golarena;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.adriangg.golarena.Dao.DaoJugador;
import com.adriangg.golarena.Dao.DaoJugadores;
import com.adriangg.golarena.Dao.DaoSobre;
import com.adriangg.golarena.Dao.DaoTienda;
import com.adriangg.golarena.Entity.Jugador;
import com.adriangg.golarena.Entity.JugadorTienda;
import com.adriangg.golarena.Entity.Jugadores;
import com.adriangg.golarena.Entity.Sobre;

@Database(
        entities = {Jugador.class, Sobre.class, JugadorTienda.class, Jugadores.class},
        version = 4
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DaoJugador daoJugador();

    public abstract DaoSobre daoSobre();
    public abstract DaoTienda daoTienda();
    public abstract DaoJugadores daoJugadores();
}
