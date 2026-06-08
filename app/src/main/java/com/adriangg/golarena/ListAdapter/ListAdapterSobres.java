package com.adriangg.golarena.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.adriangg.golarena.Entity.Sobre;
import com.adriangg.golarena.R;

import java.util.List;

public class ListAdapterSobres extends RecyclerView.Adapter<ListAdapterSobres.SobresViewHolder>{
    private Context contextActivity;
    private List<Sobre> listaSobres;
    private OnSobreClickListener listener;

    public interface OnSobreClickListener {
        void onSobreClick(Sobre sobre, ImageView imageView, TextView cantidadSorbe);
    }

    public ListAdapterSobres(List<Sobre> listaSobres, Context contextActivity, OnSobreClickListener listener){
        this.contextActivity = contextActivity;
        this.listaSobres = listaSobres;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return this.listaSobres.size();
    }

    @NonNull
    @Override
    public ListAdapterSobres.SobresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sobres_list_adapter, parent, false);
        return new ListAdapterSobres.SobresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SobresViewHolder holder, int position) {
        Sobre sobre = listaSobres.get(position);
        holder.bindData(sobre);
        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onSobreClick(sobre, holder.sobreImagen, holder.cantidadSobre);
            }
        });
    }

    public class SobresViewHolder extends RecyclerView.ViewHolder{

        ImageView sobreImagen;
        TextView nombreSobre;
        TextView cantidadSobre;

        SobresViewHolder(View itemView){
            super(itemView);
            sobreImagen = itemView.findViewById(R.id.imgSobre);
            nombreSobre = itemView.findViewById(R.id.txtNombreSobre);
            cantidadSobre = itemView.findViewById(R.id.txtCantidadSobre);
        }

        void bindData(Sobre sobre){
            nombreSobre.setText(sobre.nombre);
            cantidadSobre.setText("Cantidad: " + sobre.cantidad);
            if(sobre.tipoSobre==0){
                Glide.with(contextActivity).load(R.drawable.sobre_plata).into(sobreImagen);
            } else if (sobre.tipoSobre==1) {
                Glide.with(contextActivity).load(R.drawable.sobre_oro_premium).into(sobreImagen);
            } else if (sobre.tipoSobre==2) {
                Glide.with(contextActivity).load(R.drawable.sobre_ultimate).into(sobreImagen);
            }else{
                Glide.with(contextActivity).load(R.drawable.sobre_energizado).into(sobreImagen);
            }
        }
    }
}
