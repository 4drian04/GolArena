package com.adriangg.golarena;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;

public class Converters {

    @TypeConverter
    public static String fromArrayList(ArrayList<String> list) {
        if (list == null) return null;
        // Une los elementos con coma (o cualquier separador que no aparezca en los strings)
        return String.join(",", list);
    }
    @TypeConverter
    public static ArrayList<String> toArrayList(String data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();
        // Separa el string por comas y convierte a ArrayList
        String[] items = data.split(",");
        return new ArrayList<>(Arrays.asList(items));
    }
}
