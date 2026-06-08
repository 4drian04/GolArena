package com.adriangg.golarena.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Sobre {

    @PrimaryKey
    @NotNull
    public String nombre;

    public int cantidad;
    public int tipoSobre;
    public int jugadoresEspeciales;

    public Sobre(@NonNull String nombre, int tipoSobre){
        this.nombre=nombre;
        this.cantidad=1;
        this.tipoSobre=tipoSobre;
        this.jugadoresEspeciales=0;
    }
}
