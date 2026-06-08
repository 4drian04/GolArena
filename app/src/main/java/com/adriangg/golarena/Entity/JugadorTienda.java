package com.adriangg.golarena.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class JugadorTienda {

    @PrimaryKey
    @NotNull
    public String nombre;

    public String urlFoto;

    public int edad;

    public String nacionalidad;
    public int media;

    public JugadorTienda(@NotNull String nombre, String urlFoto, int edad, String nacionalidad, int media){
        this.nombre = nombre;
        this.urlFoto = urlFoto;
        this.edad = edad;
        this.nacionalidad = nacionalidad;
        this.media = media;
    }
}
