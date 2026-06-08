package com.adriangg.golarena.ListAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.adriangg.golarena.Entity.Jugador;
import com.adriangg.golarena.MainActivity;
import com.adriangg.golarena.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ListAdapterJugadores extends RecyclerView.Adapter<ListAdapterJugadores.JugadorViewHolder> {
    private Context contextActivity;

    private List<Jugador> listaJugadores;
    private TextView txtMonedas;

    public ListAdapterJugadores(List<Jugador> listaJugadores, Context contextActivity, TextView txtMonedas){
        this.contextActivity = contextActivity;
        this.listaJugadores = listaJugadores;
        this.txtMonedas=txtMonedas;
    }

    @Override
    public int getItemCount() {
        return this.listaJugadores.size();
    }

    @NonNull
    @Override
    public JugadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.jugadores_list_adapter, parent, false);
        return new ListAdapterJugadores.JugadorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapterJugadores.JugadorViewHolder holder, int position) {
        Jugador jugador = listaJugadores.get(position);
        holder.bindData(jugador);
        //Hacer la función del boton
        MediaPlayer soundPlayer = MediaPlayer.create(
                holder.itemView.getContext(),
                R.raw.sonido_clic_vender
        );
        MediaPlayer soldPlayer = MediaPlayer.create(
                holder.itemView.getContext(),
                R.raw.jugador_vendido
        );

        int precioJugador = (int) (MainActivity.K * Math.pow((jugador.media - 70), 2.3));
        holder.btnVender.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.btn_press));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.btn_release));
                        break;
                }
                soundPlayer.start();
                return false;
            }
        });

        holder.btnVender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoVenta(jugador, precioJugador, soldPlayer);
            }
        });
    }

    private void mostrarDialogoVenta(Jugador jugador, int valor, MediaPlayer vendido) {
        new MaterialAlertDialogBuilder(contextActivity)
                .setTitle("Venta jugador")
                .setMessage("¿Estás seguro de vender a "+ jugador.nombre +"?.\nEl valor es de " + valor + " monedas")
                .setCancelable(true)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    new Thread(() ->{
                        MainActivity.appDatabase.daoJugador().deleteJugador(jugador);
                        ((MainActivity) contextActivity).runOnUiThread(() -> {
                            eliminarJugador(jugador);
                            SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();
                            int monedas = MainActivity.sharedPreferences.getInt("monedas", 0);
                            monedas += valor;
                            txtMonedas.setText(String.valueOf(monedas));
                            preferencesEditor.putInt("monedas", monedas);
                            preferencesEditor.apply();
                            vendido.start();
                        });
                    }).start();
                })
                .show();
    }
    public void eliminarJugador(Jugador jugador) {
        int posicion = listaJugadores.indexOf(jugador);
        if (posicion != -1) {
            listaJugadores.remove(posicion);
            notifyItemRemoved(posicion);
        }
    }

    public class JugadorViewHolder extends RecyclerView.ViewHolder{

        ImageView fotoJugador;

        TextView nombreJugador;

        MaterialButton btnVender;

        JugadorViewHolder(View itemView){
            super(itemView);
            fotoJugador = itemView.findViewById(R.id.imgCarta);
            nombreJugador = itemView.findViewById(R.id.txtNombre);
            btnVender = itemView.findViewById(R.id.btnVender);
        }

        void bindData(Jugador jugador){
            try{
                nombreJugador.setText(jugador.nombre);
                Glide.with(contextActivity).load(jugador.urlFoto).into(fotoJugador);
            }catch(Exception e){
                nombreJugador.setText("Error al cargar el jugador");
                btnVender.setVisibility(View.GONE);
            }
        }
    }
}
