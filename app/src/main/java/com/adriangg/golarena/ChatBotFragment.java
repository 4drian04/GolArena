package com.adriangg.golarena;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.adriangg.golarena.ListAdapter.ListAdapterChat;
import com.adriangg.golarena.Modelos.Message;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.ProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChatBotFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private Button buttonSend;
    private ListAdapterChat adapter;

    private LlmInference llmInference;

    private StringBuilder streamingResponse = new StringBuilder();
    private int botMessagePosition = -1;
    private boolean modeloCargado = false;
    ImageView infoModelo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_bot, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recyclerViewChat);
        editText = view.findViewById(R.id.editTextMessage);
        infoModelo = view.findViewById(R.id.btnInfoChatBot);
        buttonSend = view.findViewById(R.id.buttonSend);

        adapter = new ListAdapterChat(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        boolean esAceptado = MainActivity.sharedPreferences.getBoolean("terms", false);
        if (!esAceptado){ //Comprobamos si el usuario ha aceptado los términos del chatbot de Google
            editText.setEnabled(false);
            buttonSend.setEnabled(false);
        }
        TextView textGemmaTerms = view.findViewById(R.id.textGemmaTerms);
        textGemmaTerms.setOnClickListener(v -> { //En caso de hacer clic en los términos, se muestra el Dialog con los términos correspondientes
            showGemmaTermsDialog();
            editText.setEnabled(true); //Una vez mostrado los términos, el usuario podrá escribir con el ChatBot
            buttonSend.setEnabled(true);
            SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();
            preferencesEditor.putBoolean("terms", true); //Se guarda el hecho de que el usuario ha aceptado los términos
            preferencesEditor.apply();
        });
        infoModelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog();
            }
        });
        //Esto es útil para cuando el usuario tenga el teclado abierto, los mensajes no se vean detras deñ teclado, sino que los mensajes suban
        recyclerView.setOnApplyWindowInsetsListener((v, insets) -> {
            int imeHeight = insets.getInsets(android.view.WindowInsets.Type.ime()).bottom;
            if (imeHeight > 0) {
                // teclado visible
                scrollToBottomSmooth();
            }
            return insets;
        });
        buttonSend.setOnClickListener(v -> {
            if (!modeloCargado){ //Si el modelo no está cargado, se carga el modelo
                loadModel();
                modeloCargado=true;
            }
            String userText = editText.getText().toString().trim();
            if (!userText.isEmpty()) {
                adapter.addMessage(new Message(userText, true));
                editText.setText("");
                startStreamingResponse(userText); //Se envía el mensaje al chatbot
            }
        });
    }

    /**
     * Nos permite cargar el modelo para que sea totalmente funcional
     */
    private void loadModel() {
        String modelPath = copyModelFromAssets("gemma-3-270m-it-int8.task"); //Se carga el modelo Gemma 3
        //Lo configuramos
        LlmInference.LlmInferenceOptions options =
                LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelPath)
                        .setMaxTokens(256)
                        .build();

        llmInference = LlmInference.createFromOptions(
                requireContext(),
                options
        );
    }

    /**
     * Permite enviar el prompt o pregunta que le hayamos hecho al ChatBot para que lo procese y nos responda
     * @param prompt La pregunta que le hemos hecho al ChatBot
     */
    private void startStreamingResponse(String prompt) {
        streamingResponse.setLength(0);
        // Agregamos mensaje temporal
        adapter.addMessage(new Message("Gemma está escribiendo...", false));
        botMessagePosition = adapter.getItemCount() - 1;
        String formattedPrompt =
                "You are a helpful assistant about soccer.\nUser: "
                        + prompt + "\nAssistant:";
        llmInference.generateResponseAsync(
                formattedPrompt,
                new ProgressListener<String>() {
                    @Override
                    public void run(String partialResult, boolean done) {

                        requireActivity().runOnUiThread(() -> {

                            streamingResponse.append(partialResult);

                            // Actualizamos mensaje en tiempo real
                            adapter.getMessages()
                                    .set(botMessagePosition,
                                            new Message(streamingResponse.toString(), false));

                            adapter.notifyItemChanged(botMessagePosition);
                            scrollToBottomSmooth();
                        });
                    }
                }
        );
    }
    private String copyModelFromAssets(String assetName) {
        File outFile = new File(requireContext().getFilesDir(), assetName);
        if (!outFile.exists()) {
            try (InputStream is = requireContext().getAssets().open(assetName);
                 FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outFile.getAbsolutePath();
    }

    /**
     * Se muestra en un Dialog los términos de uso de Gemma 3
     */
    private void showGemmaTermsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Términos de uso de Gemma 3")
                .setMessage(
                        "Mediante el uso de este chat, aceptas los Términos de uso de Gemma 3.\n\n" +
                                "• No distribuir Gemma ni sus derivados sin cumplir las Secciones 3 y 4.\n" +
                                "• Revisar las restricciones de uso prohibido en: https://ai.google.dev/gemma/prohibited_use_policy\n" +
                                "• Google no se hace responsable de las salidas generadas.\n\n" +
                                "Para leer todos los términos completos: https://ai.google.dev/gemma/terms"
                )
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    /**
     * Nos ayuda a subir los mensajes cuando el teclado está abierto
     */
    private void scrollToBottomSmooth() {
        recyclerView.post(() -> {
            int lastPosition = adapter.getItemCount() - 1;
            if (lastPosition >= 0) {
                // scroll suavemente al último mensaje
                ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .scrollToPositionWithOffset(lastPosition, 0);
            }
        });
    }

    /**
     * Muestra información acerca de la precisión del modelo
     */
    private void showInfoDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Importante")
                .setMessage("Es bastante probable que el ChatBot no te de información precisa. Simplemente es un añadido para responder cosas muy básicas de fútbol")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}