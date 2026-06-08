package com.adriangg.golarena;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

public class QuizFragment extends Fragment {

    private MaterialCardView cardQuizFutbol;
    private MaterialCardView cardQuizDeportes;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardQuizFutbol = view.findViewById(R.id.cardQuizFutbol);
        cardQuizDeportes = view.findViewById(R.id.cardQuizDeportes);

        //Con esto se añade animaciones a los botones
        cardQuizFutbol.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                        ((MaterialCardView) v).setCardElevation(2f);  // Baja la sombra
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        ((MaterialCardView) v).setCardElevation(12f); // Sombra original
                        break;
                }
                return false;
            }
        });
        //Si el usuario hace clic en el botón de los fútbol, muestra las tres dificultades posibles
        cardQuizFutbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarBottomSheet("FUTBOL");
            }
        });
        //Añade animación al hacer clic en el botón deportes
        cardQuizDeportes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                        ((MaterialCardView) v).setCardElevation(2f);  // Baja la sombra
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        ((MaterialCardView) v).setCardElevation(12f); // Sombra original
                        break;
                }
                return false;
            }
        });
        //Si el usuario hace clic en el botón de los deportes, muestra las tres dificultades posibles
        cardQuizDeportes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarBottomSheet("DEPORTES");
            }
        });

    }

    /**
     * Muestra los tres tipos de dificultad de las preguntas
     * @param quizTipo Tipo de quiz (Deportes o fútbol) para posteriormente iniciar el Quiz
     */
    private void mostrarBottomSheet(final String quizTipo) {
        final BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dificultad, null);

        Button btnFacil = view.findViewById(R.id.btnFacil);
        Button btnMedia = view.findViewById(R.id.btnMedia);
        Button btnDificil = view.findViewById(R.id.btnDificil);

        View.OnClickListener seleccionarDificultad = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dificultad = "";
                if (v.getId() == R.id.btnFacil) {
                    if(quizTipo.equals("FUTBOL")){ //Si el tipo de quiz es "FUTBOL", ponemos la dificultad en español, ya que en el JSON aparece en español
                        dificultad = "fácil";
                    }else{ //Si es de deportes lo ponemos en ingles porque así viene en la API
                        dificultad = "easy";
                    }
                } else if (v.getId() == R.id.btnMedia) {
                    if(quizTipo.equals("FUTBOL")){
                        dificultad = "medio";
                    }else{
                        dificultad = "medium";
                    }
                } else if (v.getId() == R.id.btnDificil) {
                    if(quizTipo.equals("FUTBOL")){
                        dificultad = "difícil";
                    }else{
                        dificultad = "hard";
                    }
                }
                dialog.dismiss();
                iniciarQuiz(quizTipo, dificultad); //Una vez elegido la dificultad, iniciamos el Quiz
            }
        };

        btnFacil.setOnClickListener(seleccionarDificultad);
        btnMedia.setOnClickListener(seleccionarDificultad);
        btnDificil.setOnClickListener(seleccionarDificultad);

        dialog.setContentView(view);
        dialog.show();
    }

    /**
     * Iniciamos el Quiz cambiando de Intent
     * @param tipo Indica el tipo de Quiz ("FUTBOL" o "DEPORTES")
     * @param dificultad Indica la dificultad del Quiz (fácil, medio, difícil)
     */
    private void iniciarQuiz(String tipo, String dificultad) {
        Intent intent = new Intent(requireContext(), QuizDeporte.class);
        intent.putExtra("TIPO", tipo);
        intent.putExtra("DIFICULTAD", dificultad);
        startActivity(intent);
    }
}