package com.adriangg.golarena.Dao;

import android.content.Intent;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.adriangg.golarena.Entity.Sobre;

import java.util.List;

@Dao
public interface DaoSobre {
    @Query("SELECT * FROM Sobre")
    List<Sobre> getSobres();
    @Query("SELECT * FROM Sobre WHERE cantidad>0")
    List<Sobre> getSobresMayorACero();
    @Insert
    void insertarSobre(Sobre...sobre);

    @Update
    int actualizarSobre(Sobre sobre);

    @Query("SELECT cantidad FROM Sobre WHERE nombre == :nombreSobre")
    Integer getCantidad(String nombreSobre);

    @Delete
    int deleteSobre(Sobre sobre);
}
