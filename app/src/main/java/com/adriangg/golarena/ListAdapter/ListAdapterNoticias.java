package com.adriangg.golarena.ListAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.adriangg.golarena.Modelos.Noticia;
import com.adriangg.golarena.R;
import java.util.List;

public class ListAdapterNoticias extends RecyclerView.Adapter<ListAdapterNoticias.NoticiaViewHolder> {

    private Context contextActivity;

    private List<Noticia> listaNoticias;


    public ListAdapterNoticias(List<Noticia> listaNoticias, Context contextActivity){
        this.listaNoticias = listaNoticias;
        this.contextActivity = contextActivity;
    }

    @Override
    public int getItemCount() {
        return this.listaNoticias.size();
    }

    @NonNull
    @Override
    public NoticiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noticias_list_adapter, parent, false);
        return new ListAdapterNoticias.NoticiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapterNoticias.NoticiaViewHolder holder, int position) {
        Noticia noticia = listaNoticias.get(position);
        holder.bindData(noticia);
        holder.fotoNoticia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri url = Uri.parse(noticia.urlNoticia);
                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                contextActivity.startActivity(intent);
            }
        });
    }

    public class NoticiaViewHolder extends RecyclerView.ViewHolder{

        TextView titular;
        TextView descripcionNoticia;
        TextView fecha;
        ImageView fotoNoticia;
        NoticiaViewHolder(View itemView){
            super(itemView);
            titular = itemView.findViewById(R.id.txtTitulo);
            descripcionNoticia = itemView.findViewById(R.id.txtDescripcion);
            fecha = itemView.findViewById(R.id.txtFecha);
            fotoNoticia = itemView.findViewById(R.id.imgNoticia);
        }

        void bindData(Noticia noticia){
            try{
                titular.setText(noticia.titular);
                descripcionNoticia.setText(noticia.descripcionNoticia);
                fecha.setText(noticia.fecha);
                Glide.with(contextActivity).load(noticia.urlImagenNoticia).into(fotoNoticia);
            }catch (Exception e){
                titular.setText("Error al cargar la noticia");
                descripcionNoticia.setText("");
                fecha.setText(noticia.fecha);
            }
        }
    }
}
