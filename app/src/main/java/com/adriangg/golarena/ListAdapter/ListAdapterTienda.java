package com.adriangg.golarena.ListAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.adriangg.golarena.Entity.Jugador;
import com.adriangg.golarena.Entity.JugadorTienda;
import com.adriangg.golarena.MainActivity;
import com.adriangg.golarena.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ListAdapterTienda extends RecyclerView.Adapter<ListAdapterTienda.TiendaViewHolder>{
    private Context contextActivity;
    private List<JugadorTienda> listaJugadores;
    private TextView txtMonedas;

    public ListAdapterTienda(List<JugadorTienda> listaJugadores, Context contextActivity, TextView txtMonedas){
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
    public ListAdapterTienda.TiendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_list_adapter, parent, false);
        return new ListAdapterTienda.TiendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TiendaViewHolder holder, int position) {
        JugadorTienda jugador = listaJugadores.get(position);
        holder.bindData(jugador);
        holder.botonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int precio = (int) (MainActivity.K * Math.pow((jugador.media - 70), 2.3));
                mostrarDialogoCompra(jugador, precio, holder.botonComprar, holder);
            }
        });
    }

    private void animarCompra(View card, View glow, MaterialButton button, FrameLayout particleContainer, Runnable onAnimationEnd) {
        // Asegurarnos de que el layout está medido
        particleContainer.post(() -> {

            // Zoom de la carta
            card.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .withEndAction(() -> card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                    );

            // Glow dorado
            glow.setAlpha(0f);
            glow.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .withEndAction(() -> glow.animate()
                            .alpha(0f)
                            .setDuration(400)
                    );

            // Partículas
            lanzarParticulas(particleContainer);

            // Cambiar botón
            button.setText("Comprado");
            button.setBackgroundColor(
                    ContextCompat.getColor(button.getContext(), R.color.green_success));
            button.setEnabled(false);
            card.postDelayed(() -> {
                card.animate()
                        .alpha(0f)
                        .translationY(-50f) // opcional: se levanta un poco al desaparecer
                        .setDuration(500)
                        .withEndAction(onAnimationEnd); // callback para actualizar DB y adapter
            }, 1000);
        });
    }

    private void lanzarParticulas(FrameLayout container) {
        container.post(() -> {
            int width = container.getWidth();
            int height = container.getHeight();

            if (width == 0 || height == 0) return;

            for (int i = 0; i < 15; i++) {

                View particle = new View(container.getContext());
                particle.setBackgroundResource(R.drawable.circle_gold_particle);

                // Tamaño aleatorio
                int size = ThreadLocalRandom.current().nextInt(8, 17);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);

                // Posición aleatoria horizontal, inicio desde botón (abajo)
                params.leftMargin = ThreadLocalRandom.current().nextInt(width / 4, 3 * width / 4);
                params.topMargin = height - 50; // un poco arriba del fondo
                container.addView(particle, params);

                // Animación: sube y desaparece
                float deltaY = ThreadLocalRandom.current().nextFloat() * -400 - 200; // altura aleatoria
                float deltaX = ThreadLocalRandom.current().nextFloat() * 200 - 100;   // ligera dispersión horizontal

                particle.animate()
                        .translationYBy(deltaY)
                        .translationXBy(deltaX)
                        .alpha(0f)
                        .setDuration(1000)
                        .withEndAction(() -> container.removeView(particle));
            }
        });
    }



    private void mostrarDialogoCompra(JugadorTienda jugador, int valor, MaterialButton button, TiendaViewHolder holder) {
        new MaterialAlertDialogBuilder(contextActivity)
                .setTitle("Venta jugador")
                .setMessage("¿Estás seguro de comprar a "+ jugador.nombre +"?")
                .setCancelable(true)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    if(valor>MainActivity.sharedPreferences.getInt("monedas", 0)){
                        mostrarDialogoErrorCompra();
                    }else{
                        animarCompra(holder.itemView, holder.glow, holder.botonComprar, holder.layoutParticulas, () ->{
                            //Actualizar base de datos y monedas
                            new Thread(() -> {
                                Jugador jugadorNuevo = new Jugador(jugador.nombre, jugador.urlFoto, jugador.edad, jugador.nacionalidad, jugador.media);
                                MainActivity.appDatabase.daoJugador().insertarJugador(jugadorNuevo);
                                MainActivity.appDatabase.daoTienda().deleteJugadorComprado(jugador.nombre);

                                ((MainActivity) contextActivity).runOnUiThread(() -> {
                                    SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();
                                    int monedas = MainActivity.sharedPreferences.getInt("monedas", 0);
                                    monedas -= valor;
                                    txtMonedas.setText(String.valueOf(monedas));
                                    preferencesEditor.putInt("monedas", monedas);
                                    preferencesEditor.apply();
                                    // Marcamos botón comprado
                                    button.setEnabled(false);
                                });
                            }).start();
                        });
                    }
                })
                .show();
    }

    private void mostrarDialogoErrorCompra() {
        new MaterialAlertDialogBuilder(contextActivity)
                .setTitle("Venta jugador")
                .setMessage("No tienes suficientes monedas para comprar al jugador")
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public class TiendaViewHolder extends RecyclerView.ViewHolder{

        ImageView fotoJugador;
        TextView precioJugador;
        MaterialButton botonComprar;
        FrameLayout layoutParticulas;
        View glow;

        TiendaViewHolder(View itemView){
            super(itemView);
            fotoJugador = itemView.findViewById(R.id.imgJugador);
            precioJugador = itemView.findViewById(R.id.txtPrecio);
            botonComprar = itemView.findViewById(R.id.btnComprar);
            layoutParticulas = itemView.findViewById(R.id.layoutParticulas);
            glow = itemView.findViewById(R.id.viewCompraGlow);
        }

        void bindData(JugadorTienda jugador){
            try{
                int precio = (int) (MainActivity.K * Math.pow((jugador.media - 70), 2.3));
                precioJugador.setText(String.valueOf(precio));
                Glide.with(contextActivity).load(jugador.urlFoto).into(fotoJugador);
            }catch(Exception e){
                Glide.with(contextActivity).load(R.drawable.jugador).into(fotoJugador);
                precioJugador.setText("ERROR");
                botonComprar.setEnabled(false);
            }
        }
    }
}
