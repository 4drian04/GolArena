package com.adriangg.golarena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.media.MediaPlayer;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.adriangg.golarena.Entity.Sobre;
import com.adriangg.golarena.Modelos.Pregunta;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizDeporte extends AppCompatActivity {

    private int NUMERO_PREGUNTAS = 5;
    private final int NUMERO_VIDAS = 3;
    MaterialCardView opcion1;
    MaterialCardView opcion2;
    MaterialCardView opcion3;
    MaterialCardView opcion4;

    LinearLayout progressBarLayout;
    LinearLayout contenidoQuizLayout;
    LinearLayout opcionesQuizLayout;
    TextView tituloPregunta;
    TextView txtOpcion1;
    TextView txtOpcion2;
    TextView txtOpcion3;
    TextView txtOpcion4;
    TextView txtNumeroPregunta;
    String respuestaCorrecta;
    String respuestasElegida;
    ImageView error1;
    ImageView error2;
    ImageView error3;
    TextView textoVidas;
    ProgressBar progressBar;
    private int numeroErrores;
    private int numeroAciertos;
    private  int numeroPregunta;
    private String dificultad;
    private ArrayList<Pregunta> preguntasDeportes;
    private String tipoQuiz;
    Translator englishSpanishTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_deporte);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        opcion1 = findViewById(R.id.opcion1);
        opcion2 = findViewById(R.id.opcion2);
        opcion3 = findViewById(R.id.opcion3);
        opcion4 = findViewById(R.id.opcion4);
        error1 = findViewById(R.id.error1);
        error2 = findViewById(R.id.error2);
        error3 = findViewById(R.id.error3);
        progressBarLayout = findViewById(R.id.menuProgressBarPreguntaLayout);
        contenidoQuizLayout = findViewById(R.id.contenidoQuiz);
        opcionesQuizLayout = findViewById(R.id.opcionesLayout);
        tituloPregunta = findViewById(R.id.txtPregunta);
        txtOpcion1 = findViewById(R.id.txtOpcion1);
        txtOpcion2 = findViewById(R.id.txtOpcion2);
        txtOpcion3 = findViewById(R.id.txtOpcion3);
        txtOpcion4 = findViewById(R.id.txtOpcion4);
        txtNumeroPregunta = findViewById(R.id.txtNumeroPregunta);
        textoVidas = findViewById(R.id.vidaInformativo);
        progressBar = findViewById(R.id.progresoPregunta);
        numeroErrores=0;
        numeroAciertos=0;
        numeroPregunta=0;
        preguntasDeportes = new ArrayList<>();

        Intent intent = getIntent();
        dificultad = intent.getStringExtra("DIFICULTAD"); //Obtenemos la dificultad elegida
        if(dificultad.equals("fácil") || dificultad.equals("medio")){  //En caso de que sea de fútbol y la dificultad sea fácil o medio, el número de preguntas será 10
            this.NUMERO_PREGUNTAS=10;
        }
        //Obtenemos el boolean para comprobar si el traductor está descargado
        boolean traductorDisponible = MainActivity.sharedPreferences.getBoolean("traductorDisponible", false);
        //Configuramos el traductor del inglés al español
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.SPANISH)
                        .build();
        //Creamos el traductor
        englishSpanishTranslator =
                Translation.getClient(options);
        if(!traductorDisponible){ //En caso de que no tengamos el traductor descargado, lo descargamos
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();
            englishSpanishTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //Una vez descargado, guardamos el boolean que indica que se descargó el traductor
                    SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();
                    preferencesEditor.putBoolean("traductorDisponible", true);
                    preferencesEditor.apply();
                }
            });
        }
        tipoQuiz = intent.getStringExtra("TIPO");
        if(tipoQuiz != null && tipoQuiz.equals("FUTBOL")){ //Si el tipo de Quiz es "FUTBOL" mostramos las preguntsa de fútbol
            preguntasFutbol(dificultad, true);
        } else if (tipoQuiz!=null && tipoQuiz.equals("DEPORTES")) { //Si es de deportes, mostramos las preguntas de deportes
            preguntasDeporte(dificultad);
        }else{ //Si por algún casual no es ninguno de los anteriores, informamos del error y hacemos un finish() del activity
            Toast.makeText(this, "Ha ocurrido un error, intentelo de nuevo", Toast.LENGTH_LONG).show();
            finish();
        }
        //Si el usuario hace clic en la opcion1, comprobamos la respuesta. Además, se le añade una pequeña animación y un sonido
        opcion1.setOnClickListener(view -> {
            animarOpcion(opcion1, () -> {
                respuestasElegida = String.valueOf(txtOpcion1.getText());
                comprobarRespuesta();
            });
        });
        //Lo mismo para la segunda opción
        opcion2.setOnClickListener(view -> {
            animarOpcion(opcion2, () -> {
                respuestasElegida = String.valueOf(txtOpcion2.getText());
                comprobarRespuesta();
            });
        });
        //Lo mismo para la tercera opción
        opcion3.setOnClickListener(view -> {
            animarOpcion(opcion3, () -> {
                respuestasElegida = String.valueOf(txtOpcion3.getText());
                comprobarRespuesta();
            });
        });
        //Lo mismo para la cuarta opción
        opcion4.setOnClickListener(view -> {
            animarOpcion(opcion4, () -> {
                respuestasElegida = String.valueOf(txtOpcion4.getText());
                comprobarRespuesta();
            });
        });
    }

    private void comprobarRespuesta(){
        if(respuestasElegida.equals(respuestaCorrecta)){ //Comprobamos si la respuesta elegida es igual a la correcta
            numeroAciertos++; //En caso de ser así, aumentamos el número de aciertos
            MediaPlayer gol = MediaPlayer.create( //Creamos un sonido en caso de que acierte
                    this,
                    R.raw.gol
            );
            gol.start(); //Emitimos el sonido
            if(this.numeroPregunta<this.NUMERO_PREGUNTAS){ //Comprobamos si hay que seguir mostrando nuevas preguntas
                if(tipoQuiz.equals("FUTBOL")){ //En caso de ser así, comprobamos que tipo de Quiz es
                    preguntasFutbol(dificultad, true); //Si es de fútbol, mostramos una nueva pregunta de fútbol
                }else{ //Si es de deportes, mostramos una nueva pregunta de deporte en general
                    nuevaPregunta();
                }
            }else{ //En caso de que ya no haya más preguntas que mostrar, le informamos al jugador que ya ha ganado y le damos las recompensas correspondientes
                mostrarDialogoVictoria(dificultad);
            }
        }else{ //En caso de que no acierte aumentamos el número de errores
            numeroErrores++;
            MediaPlayer arbitro = MediaPlayer.create( //Creamos un sonido en caso de que falle
                    this,
                    R.raw.arbitro_futbol
            );
            arbitro.start(); //Emitimos el sonido
            if(numeroErrores==1){ //En caso de que el usuario tenga un error, le informamos de ello con un símbolo en la parte inferior de la pantalla
                error1.setVisibility(View.VISIBLE);
                if(tipoQuiz.equals("FUTBOL")){ //Como no ha llegado a los tres errores, omstramos una nueva pregunta de fútbol
                    preguntasFutbol(dificultad, false);
                }else{ //en caso de que no sea de fútbol, le mostramos de deportes
                    nuevaPregunta();
                }
            } else if (numeroErrores==2) { //Lo mismo con dos errores
                error2.setVisibility(View.VISIBLE);
                if(tipoQuiz.equals("FUTBOL")){
                    preguntasFutbol(dificultad, false);
                }else{
                    nuevaPregunta();
                }
            }else{ //En caso de que llegue a tres errores, informamos al usuario de que ha perdido
                error3.setVisibility(View.VISIBLE);
                mostrarDialogoDerrota();
            }
            textoVidas.setText("Te quedan " + (NUMERO_VIDAS-numeroErrores) + " vidas");
        }
    }

    /**
     * Mostramos un Dialog donde se informe la derrota. Además, se elimina el Activity de la pila
     */
    private void mostrarDialogoDerrota() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Has perdido 😢")
                .setMessage("Te has quedado sin intentos.\n¿Quieres volver a intentarlo?")
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    /**
     * Se muestra un Dialog de victoria y se le proporciona las recompensas al usuario dependiendo de la dificultad
     * @param dificultad Dificultad del quiz, que dependiendo que valor tenga, se obtiene una recompensa u otra
     */
    private void mostrarDialogoVictoria(String dificultad) {

        new Thread(() -> {

            String recompensa;
            int monedas;
            int tipoSobre;
            boolean jugadorEspecial = false;
            //En caso de que la dificultad sea fácil, le daremos 10000 monedas y un sobre de plata
            if (dificultad.equals("fácil") || dificultad.equals("easy")) {
                recompensa = "sobre de plata";
                monedas = 10000;
                tipoSobre = 0;
            } else if (dificultad.equals("medio") || dificultad.equals("medium")) { //Si es medio, 20000 de oro y un sobre de oro
                recompensa = "sobre oro";
                monedas = 20000;
                tipoSobre = 1;
            } else { //En caso de que sea en difícil, se le otorga 30000 monedas y dependiendo de la suerte, un sobre u otro
                monedas = 30000;
                Random random = new Random();
                int numeroRandom = random.nextInt(2); //Hacemos un random
                if (numeroRandom == 0) { //Si el número es 0, se le da un sobre ultimate
                    recompensa = "sobre ultimate";
                    tipoSobre = 2;
                } else { //En caso contrario un sobre energizante (mejor que el ultimate, con posibilidad de conseguir jugadores especiales)
                    recompensa = "sobre energizante";
                    tipoSobre = 3;
                    if(tipoQuiz.equals("DEPORTES")){
                        jugadorEspecial=true;
                    }
                }
            }

            // Monedas
            int monedasUsuario = MainActivity.sharedPreferences.getInt("monedas", 0);
            monedasUsuario += monedas;

            SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();
            preferencesEditor.putInt("monedas", monedasUsuario);
            preferencesEditor.apply();

            // Comprobamos si hay sobres de ello
            Integer cantidadSobre = MainActivity.appDatabase
                    .daoSobre()
                    .getCantidad(recompensa);

            Sobre sobre = new Sobre(recompensa, tipoSobre);
            if(jugadorEspecial){
                sobre.jugadoresEspeciales++;
            }
            if (cantidadSobre == null) { //En caso de que no haya, insertamos el sobre
                MainActivity.appDatabase.daoSobre().insertarSobre(sobre);
            } else { //En caso de que exista, se aumenta la cantidad y se actualiza el sobre
                sobre.cantidad = cantidadSobre + 1;
                MainActivity.appDatabase.daoSobre().actualizarSobre(sobre);
            }

            // Volvemos al hilo principal para mostrar diálogo
            runOnUiThread(() -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Has ganado 🥳")
                        .setMessage("Has ganado un " + recompensa + " y " + monedas + " monedas")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .show();
            });

        }).start();
    }

    /**
     * Una vez el usuario responda a una pregunta del tipo "DEPORTES", se muestra otra nueva pregunta
     */
    private void nuevaPregunta(){
        try{
            Pregunta pregunta = preguntasDeportes.get(numeroPregunta);
            mostrarPregunta(pregunta.getQuestion(), pregunta.getOptions(), pregunta.getCorrectAnswer());
        }catch(IndexOutOfBoundsException e){
            preguntasDeporte(dificultad); //En caso de que de una expceción, se llama de nuevo a la API de preguntas de deportes para recargar la lista de preguntas
        }
    }

    /**
     * Se obtiene una pregunta de fútbol de JSON que está en el asset
     * @param dificultad La pregunta que se muestra depende de la dificultad
     * @param incrementarPregunta Indica si se aumenta o no el número de preguntas
     */
    private void preguntasFutbol(String dificultad, boolean incrementarPregunta){
        try {
            opcionesQuizLayout.setVisibility(View.GONE);
            tituloPregunta.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.VISIBLE);
            String json = cargarJsonDesdeAssets("quizFootball.json");

            JSONArray jsonArray = new JSONArray(json);
            List<JSONObject> preguntasFiltradas = new ArrayList<>();

            // Filtrar por dificultad
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pregunta = jsonArray.getJSONObject(i);

                if (pregunta.getString("nivel").equalsIgnoreCase(dificultad)) {
                    preguntasFiltradas.add(pregunta);
                }
            }

            // Elegir pregunta aleatoria
            Random random = new Random();
            JSONObject preguntaAleatoria =
                    preguntasFiltradas.get(random.nextInt(preguntasFiltradas.size()));

            // Obtener datos
            String textoPregunta = preguntaAleatoria.getString("pregunta");
            JSONArray opcionesJson = preguntaAleatoria.getJSONArray("opciones");
            String respuestaCorrecta = preguntaAleatoria.getString("respuestaCorrecta");

            //Obtenemos las distintas opciones de las preguntas
            List<String> opciones = new ArrayList<>();
            for (int i = 0; i < opcionesJson.length(); i++) {
                opciones.add(opcionesJson.getString(i));
            }

            Log.d("QUIZ", "Pregunta: " + textoPregunta);
            Log.d("QUIZ", "Opciones: " + opciones);
            Log.d("QUIZ", "Correcta: " + respuestaCorrecta);

            mostrarPregunta(textoPregunta, opciones, respuestaCorrecta); //Mostramos por pantalla la pregunta
            progressBarLayout.setVisibility(View.GONE);
            tituloPregunta.setVisibility(View.VISIBLE);
            opcionesQuizLayout.setVisibility(View.VISIBLE);
            if(incrementarPregunta){
                this.numeroPregunta++;
            }
        } catch (Exception e) { //Si ocurre algún tipo de error, se lo hacemos saber al usuario
            Toast.makeText(this, "Ha ocurrido un error, intentelo de nuevo", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Obtiene y muestra las preguntas de deportes ("DEPORTE")
     * @param dificultad Dependiendo de la dificultad, se muestra unas preguntas u otras
     */
    private void preguntasDeporte(String dificultad){
        try {
            opcionesQuizLayout.setVisibility(View.GONE);
            tituloPregunta.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.VISIBLE);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url;
            progressBar.setProgress(25);
            if(dificultad.equals("easy")){ //Si la dificultad es "easy", lo indicamos en la API
                url = "https://opentdb.com/api.php?amount=5&category=21&difficulty=" + dificultad;
            }else{ //Si no es EASY, el número de preguntas incrementará a 10
                this.NUMERO_PREGUNTAS=10;
                url = "https://opentdb.com/api.php?amount=10&category=21&difficulty=" + dificultad;
            }
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        int responseCode = jsonObject.getInt("response_code");
                        if (responseCode != 0) return;

                        JSONArray results = jsonObject.getJSONArray("results");

                        ArrayList<Pregunta> listaPreguntas = new ArrayList<>();
                        progressBar.setProgress(50);
                        for (int i = 0; i < results.length(); i++) { //Recorremos los distintos resultados

                            JSONObject obj = results.getJSONObject(i);
                            //Obtenemos los distintos datos
                            String question = obj.getString("question");
                            String correctAnswer = obj.getString("correct_answer");
                            String difficultyJson = obj.getString("difficulty");
                            String category = obj.getString("category");

                            // Opciones
                            ArrayList<String> opciones = new ArrayList<>();
                            JSONArray incorrectAnswers = obj.getJSONArray("incorrect_answers");

                            for (int j = 0; j < incorrectAnswers.length(); j++) {
                                opciones.add(incorrectAnswers.getString(j));
                            }

                            opciones.add(correctAnswer);
                            Collections.shuffle(opciones); // Mezclar respuestas

                            Pregunta pregunta = new Pregunta(
                                    Html.fromHtml(question).toString(),
                                    Html.fromHtml(correctAnswer).toString(),
                                    opciones,
                                    difficultyJson,
                                    category
                            );

                            listaPreguntas.add(pregunta);
                        }
                        progressBar.setProgress(100);
                        preguntasDeportes = listaPreguntas;
                        //Mostramos la pregunta correspondiente
                        mostrarPregunta(preguntasDeportes.get(0).getQuestion(), preguntasDeportes.get(0).getOptions(), preguntasDeportes.get(0).getCorrectAnswer());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) { //En caso de error se lo hacemos saber al usuario
                    Toast.makeText(QuizDeporte.this, "Ha ocurrido un error, intentelo de nuevo", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) { //En caso de error se lo hacemos saber al usuario
            Toast.makeText(this, "Ha ocurrido un error, intentelo de nuevo", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Mostramos el número de la pregunta por el que va el usuario por pantalla
     * @param numeroPregunta Indica el número de la pregunta donde se encuentra el usuario en el Quiz
     */
    private void mostrarNumeroPregunta(int numeroPregunta){
        StringBuilder stringBuilder = new StringBuilder("Pregunta ");
        stringBuilder.append(numeroPregunta);
        stringBuilder.append(" de ");
        stringBuilder.append(this.NUMERO_PREGUNTAS);
        txtNumeroPregunta.setText(stringBuilder);
    }

    /**
     * Carga el JSON desde el archivo indicado por parámetro
     * @param filename Archivo a cargar
     * @return
     */
    private String cargarJsonDesdeAssets(String filename) {
        String json;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Se muestra la pregunta y las distintas opciones por pantalla
     * @param textoPregunta Título de la pregunta
     * @param opciones Las distintas opciones de la pregunta
     * @param respuestaCorrecta Respuesta correcta de la pregunta
     */
    private void mostrarPregunta(String textoPregunta, List<String> opciones, String respuestaCorrecta){
        if(tipoQuiz.equals("DEPORTES")){
            Log.d("XXXX", respuestaCorrecta);
            numeroPregunta++;
        }
        // Traducir la pregunta si el traductor está disponible
        if(englishSpanishTranslator != null && tipoQuiz.equals("DEPORTES")){
            englishSpanishTranslator.translate(textoPregunta)
                    .addOnSuccessListener(translatedText -> {
                        // Mostrar la pregunta traducida
                        tituloPregunta.setText(translatedText);
                        progressBarLayout.setVisibility(View.GONE);
                        tituloPregunta.setVisibility(View.VISIBLE);
                        opcionesQuizLayout.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        // Si falla la traducción, mostrar la original
                        tituloPregunta.setText(textoPregunta);
                        progressBarLayout.setVisibility(View.GONE);
                        tituloPregunta.setVisibility(View.VISIBLE);
                        opcionesQuizLayout.setVisibility(View.VISIBLE);
                    });
        } else {
            // Si el traductor no está listo, mostrar la original
            tituloPregunta.setText(textoPregunta);
        }
        txtOpcion1.setText(opciones.get(0));
        txtOpcion2.setText(opciones.get(1));
        if(opciones.size()>2){ //Si hay más de dos opciones, se muestran las otras opciones
            opcion3.setVisibility(View.VISIBLE);
            opcion4.setVisibility(View.VISIBLE);
            txtOpcion3.setText(opciones.get(2));
            txtOpcion4.setText(opciones.get(3));
        }else{ //En caso contrario, se quita dichos botones para que el usuario no pueda interactuar con ellos
            opcion3.setVisibility(View.GONE);
            opcion4.setVisibility(View.GONE);
        }
        this.respuestaCorrecta = respuestaCorrecta;
        mostrarNumeroPregunta(numeroPregunta);
    }

    /**
     * Animación al hacer clic en el botón
     * @param card
     * @param onEndAction
     */
    private void animarOpcion(MaterialCardView card, Runnable onEndAction) {
        card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .translationY(8f)
                .setDuration(100)
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(100)
                            .withEndAction(onEndAction)
                            .start();
                })
                .start();
    }

}