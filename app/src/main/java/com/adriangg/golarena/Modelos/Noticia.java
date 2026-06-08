package com.adriangg.golarena.Modelos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Noticia {

    public String titular;

    public String descripcionNoticia;

    public String fecha;

    public String urlImagenNoticia;
    public String urlNoticia;

    public Noticia(String titular, String descripcionNoticia, String fecha, String url, String urlNoticia){
        this.titular = titular;
        this.descripcionNoticia = descripcionNoticia;
        this.urlImagenNoticia = url;
        // Formato de entrada (API)
        SimpleDateFormat inputFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.urlNoticia = urlNoticia;
        // Formato de salida (para mostrar)
        SimpleDateFormat outputFormat =
                new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        try{
            Date date = inputFormat.parse(fecha);
            this.fecha = outputFormat.format(date);
        }catch (ParseException e){
            this.fecha = "Última Hora";
        }
    }
}
