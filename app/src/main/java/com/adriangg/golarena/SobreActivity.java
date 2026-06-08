package com.adriangg.golarena;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.adriangg.golarena.Entity.Jugador;
import com.adriangg.golarena.Entity.Jugadores;
import com.adriangg.golarena.Entity.Sobre;
import com.adriangg.golarena.ListAdapter.ListAdapterSobres;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SobreActivity extends AppCompatActivity {

    RecyclerView recyclerViewSobres;
    LinearLayout progressBarLinearLayout;
    LinearLayout errorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sobre);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerViewSobres = findViewById(R.id.recyclerSobres);
        recyclerViewSobres.setLayoutManager(new LinearLayoutManager(this));
        progressBarLinearLayout = findViewById(R.id.menuProgressBarSobresLayout);
        errorLayout = findViewById(R.id.errorSobresLayout);
        progressBarLinearLayout.setVisibility(View.VISIBLE);
        recyclerViewSobres.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        new Thread(() -> {

            List<Sobre> sobres = MainActivity.appDatabase
                    .daoSobre()
                    .getSobresMayorACero();

            runOnUiThread(() -> {

                progressBarLinearLayout.setVisibility(View.GONE);

                if (sobres.isEmpty()) {
                    errorLayout.setVisibility(View.VISIBLE);
                } else {

                    ListAdapterSobres adapter = new ListAdapterSobres(
                            sobres,
                            this,
                            (sobre, imageView, cantidadSobre) -> {

                                if (sobre.cantidad > 0) {
                                    MediaPlayer abrirSobreAudio = MediaPlayer.create(
                                            this,
                                            R.raw.abrir_sobre
                                    );
                                    abrirSobreAudio.start();
                                    imageView.animate()
                                            .scaleX(1.3f)
                                            .scaleY(1.3f)
                                            .rotationYBy(360f)
                                            .setDuration(800)
                                            .withEndAction(() -> {
                                                //hacerFlashBlanco();
                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                    obtenerJugadorAleatorio(sobre, cantidadSobre);
                                                }, 150);
                                            })
                                            .start();

                                } else {
                                    Toast.makeText(
                                            SobreActivity.this,
                                            "No tienes más sobres de este tipo",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });

                    recyclerViewSobres.setAdapter(adapter);
                    recyclerViewSobres.setVisibility(View.VISIBLE);
                }
            });

        }).start();
    }
    private void obtenerJugadorAleatorio(Sobre sobre, TextView cantidadSobre) {

        new Thread(() -> {
            Random random = new Random();
            // Obtener todos los jugadores
            List<Jugadores> todos = MainActivity.appDatabase.daoJugadores().getJugadores();
            int posibilidadJugadorEspecial=0;
            if (todos.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No hay jugadores disponibles", Toast.LENGTH_LONG).show()
                );
                return;
            }

            int mediaMinima;

            if (sobre.tipoSobre == 0) {          // Plata
                mediaMinima = 65;
            } else if (sobre.tipoSobre == 1) {   // Oro
                mediaMinima = 74;
            } else if (sobre.tipoSobre == 2) {   // Ultimate
                mediaMinima = 80;
            } else {                             // Energizante
                mediaMinima = 85;
                posibilidadJugadorEspecial = random.nextInt(10);
            }
            if(posibilidadJugadorEspecial<=6 && sobre.jugadoresEspeciales<=0){
                // Filtrar por media
                List<Jugadores> filtrados = new ArrayList<>();
                for (Jugadores j : todos) {
                    if (j.media > mediaMinima) {
                        filtrados.add(j);
                    }
                }

                if (filtrados.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No hay jugadores para este sobre", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // Elegir uno aleatorio
                Collections.shuffle(filtrados);
                Jugadores elegido = filtrados.get(0);

                // Convertir a tu entidad Jugador
                Jugador jugador = new Jugador(
                        elegido.nombre,
                        elegido.urlFoto,
                        elegido.edad,
                        elegido.nacionalidad,
                        elegido.media
                );
                guardarJugador(jugador, sobre, cantidadSobre);
            }else{
                int numeroPaginaAleatorio = random.nextInt(8)+1; //Como el número de páginas no puede ser 0, le sumamos 1
                String url = "https://futevolution.com/api/players?page=" + numeroPaginaAleatorio;
                RequestQueue queue = Volley.newRequestQueue(this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            ArrayList<Jugador> listaPlayers = new ArrayList<>();

                            JSONArray dataArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < dataArray.length(); i++) {

                                JSONObject playerObj = dataArray.getJSONObject(i);

                                String commonName = playerObj.optString("commonName");
                                int age = playerObj.optInt("age");
                                int overall = playerObj.optInt("overall");
                                String nationality = playerObj.optString("nationality");
                                String cardImageUrl = playerObj.optString("cardImageUrl");

                                listaPlayers.add(
                                        new Jugador(commonName, cardImageUrl, age, nationality, overall)
                                );
                            }
                            Collections.shuffle(listaPlayers);
                            Jugador elegido = listaPlayers.get(0);
                            if(sobre.jugadoresEspeciales>0){
                                sobre.jugadoresEspeciales--;
                            }
                            new Thread(() -> {
                                guardarJugador(elegido, sobre, cantidadSobre);
                            }).start();
                        } catch (JSONException e) {
                            Toast.makeText(SobreActivity.this, "Ha ocurrido un error al abrir el sobre", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        List<Jugadores> filtrados = new ArrayList<>();
                        for (Jugadores j : todos) {
                            if (j.media > mediaMinima) {
                                filtrados.add(j);
                            }
                        }

                        if (filtrados.isEmpty()) {
                            runOnUiThread(() ->
                                    Toast.makeText(SobreActivity.this, "No hay jugadores para este sobre", Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        // Elegir uno aleatorio
                        Collections.shuffle(filtrados);
                        Jugadores elegido = filtrados.get(0);

                        // Convertir a tu entidad Jugador
                        Jugador jugador = new Jugador(
                                elegido.nombre,
                                elegido.urlFoto,
                                elegido.edad,
                                elegido.nacionalidad,
                                elegido.media
                        );
                        new Thread(() -> {
                            guardarJugador(jugador, sobre, cantidadSobre);
                        }).start();
                    }
                }){
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        try {
                            // Forzamos UTF-8
                            String utf8String = new String(response.data, "UTF-8");
                            return Response.success(utf8String, getCacheEntry());
                        } catch (Exception e) {
                            return Response.error(new ParseError(e));
                        }
                    }
                };
                queue.add(stringRequest);
            };

        }).start();
    }

    private void guardarJugador(Jugador jugador, Sobre sobre, TextView cantidadSobre){
        // Guardar en BD
        MainActivity.appDatabase.daoJugador().insertarJugador(jugador);
        // Restar sobre
        sobre.cantidad -= 1;
        runOnUiThread(() -> cantidadSobre.setText("Cantidad: " + sobre.cantidad));
        MainActivity.appDatabase.daoSobre().actualizarSobre(sobre);

        runOnUiThread(() -> {
            ViewGroup root = findViewById(android.R.id.content);
            View flashView = new View(this);
            flashView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            flashView.setBackgroundColor(jugador.media >= 90 ? Color.parseColor("#FFD700") : Color.WHITE);
            flashView.setAlpha(0f);
            root.addView(flashView);

            flashView.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .withEndAction(() ->
                            new Handler(Looper.getMainLooper()).postDelayed(() ->
                                            flashView.animate()
                                                    .alpha(0f)
                                                    .setDuration(400)
                                                    .withEndAction(() -> {
                                                        root.removeView(flashView);
                                                        mostrarJugadorDialog(jugador);
                                                    })
                                                    .start()
                                    , 200)
                    ).start();
        });
    }

    private void mostrarJugadorDialog(Jugador jugador){
        MediaPlayer jugadorAudio = MediaPlayer.create(
                this,
                R.raw.jugador_sobre
        );
        jugadorAudio.start();
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_jugador);
        ImageView img = dialog.findViewById(R.id.imgJugador);
        TextView nombre = dialog.findViewById(R.id.txtNombreJugador);
        View card = dialog.findViewById(R.id.cardJugador);
        View particles = dialog.findViewById(R.id.viewParticles);
        MaterialButton btnAceptarJugador = dialog.findViewById(R.id.btnAceptar);

        nombre.setText(jugador.nombre);

        Glide.with(this)
                .load(jugador.urlFoto)
                .into(img);

        // cambia de fondo según la rareza
        if(jugador.media >= 88){
            particles.setBackgroundResource(R.drawable.bg_particles_gold);
            particles.setAlpha(0.6f); // hacer partículas visibles
            if(jugador.media >= 90){
                card.setElevation(30f);
            }
        } else {
            particles.setBackgroundResource(R.drawable.bg_particles_blue);
            particles.setAlpha(0.4f);
        }
        dialog.setCancelable(true);
        dialog.show();

        // Aparecer la carta con rebote + shake
        card.setScaleX(0f);
        card.setScaleY(0f);
        card.setAlpha(0f);
        card.setRotation(0f);

        card.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .rotationBy(randomShakeAngle())
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> card.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(0f)
                        .setDuration(200)
                        .start())
                .start();

        // Partículas detrás de la carta según rareza
        if (jugador.media >= 88) {
            showParticles(dialog.getWindow().getDecorView(), card);
        }

        // Animar nombre con fade + scale + flicker dorado si media >= 90
        nombre.setAlpha(0f);
        nombre.setScaleX(0f);
        nombre.setScaleY(0f);
        nombre.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    if (jugador.media >= 90) {
                        flickerNombre(nombre);
                    }
                })
                .start();

        // Botón aceptar
        btnAceptarJugador.setOnClickListener(v -> {
            card.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(400).withEndAction(dialog::dismiss).start();
            particles.animate().alpha(0f).setDuration(400).start();
            if (jugadorAudio.isPlaying()) jugadorAudio.stop();
        });
    }

    private void flickerNombre(TextView nombre) {
        nombre.animate().alpha(0.3f).setDuration(100).withEndAction(() ->
                nombre.animate().alpha(1f).setDuration(100).withEndAction(() ->
                        nombre.animate().alpha(0.5f).setDuration(100).withEndAction(() ->
                                nombre.animate().alpha(1f).setDuration(100).start()
                        ).start()
                ).start()
        ).start();
    }

    private float randomShakeAngle() {
        Random r = new Random();
        return r.nextBoolean() ? 10f : -10f;
    }

    private void showParticles(View container, View cardJugador) {

        FrameLayout particleContainer = container.findViewById(R.id.particlesContainer);

        // Esperamos a que el layout tenga tamaño
        particleContainer.post(() -> {

            Random random = new Random();

            for (int i = 0; i < 80; i++) {

                ImageView particle = new ImageView(this);
                particle.setImageResource(R.drawable.particle_circle);

                int size = 12 + random.nextInt(14); // entre 6dp y 12dp
                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(dpToPx(size), dpToPx(size));
                float centerX = cardJugador.getX() + cardJugador.getWidth() / 2f;
                float centerY = cardJugador.getY() + cardJugador.getHeight() / 2f;

                particle.setX(centerX);
                particle.setY(centerY);
                particle.setLayoutParams(params);

                particleContainer.addView(particle);


                // Dirección aleatoria en 360°
                float distance = 400 + random.nextInt(300);

                // Dirección izquierda o derecha
                boolean derecha = random.nextBoolean();
                double angle = Math.toRadians(random.nextInt(360));
                float xOffset = (float) (Math.cos(angle) * distance);
                float yOffset = (float) (Math.sin(angle) * distance);
// Pequeña variación vertical

                particle.animate()
                        .translationXBy(xOffset)
                        .translationYBy(yOffset)
                        .alpha(0f)
                        .setDuration(900 + random.nextInt(400))
                        .withEndAction(() -> particleContainer.removeView(particle))
                        .start();
            }
        });
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}