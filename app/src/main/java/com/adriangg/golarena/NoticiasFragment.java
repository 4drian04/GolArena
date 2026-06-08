package com.adriangg.golarena;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.adriangg.golarena.ListAdapter.ListAdapterNoticias;
import com.adriangg.golarena.Modelos.Noticia;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class NoticiasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListAdapterNoticias listAdapterNoticias;
    private LinearLayout linearLayoutRecyclerView;

    private LinearLayout linearLayoutProgressBar;
    private LinearLayout linearLayoutError;
    private ImageView infoNoticias;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_noticias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerNoticias);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        linearLayoutRecyclerView = view.findViewById(R.id.noticiasRecyclerViewLayout);
        linearLayoutProgressBar = view.findViewById(R.id.menuProgressBarNoticiasLayout);
        linearLayoutError = view.findViewById(R.id.errorNoticiaLayout);
        linearLayoutRecyclerView.setVisibility(View.GONE);
        linearLayoutProgressBar.setVisibility(View.VISIBLE);
        infoNoticias = view.findViewById(R.id.btnInfoNoticias);
        queue = Volley.newRequestQueue(requireContext());
        //Llamamos a la API que nos devuelve las últimas noticias
        StringRequest stringRequest = new StringRequest(Request.Method.GET, MainActivity.urlNoticiasEspanha, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    ArrayList<Noticia> listaNoticias = new ArrayList<>();

                    JSONArray articlesArray = jsonObject.getJSONArray("articles"); //Leemos las noticias obtenidas

                    for (int i = 0; i < articlesArray.length(); i++) {

                        JSONObject article = articlesArray.getJSONObject(i);

                        String headline = article.getString("headline");
                        String description = article.getString("description");
                        String published = article.getString("published");
                        // Primera imagen
                        String imageUrl = "";
                        JSONArray imagesArray = article.getJSONArray("images");
                        if (imagesArray.length() > 0) {
                            imageUrl = imagesArray.getJSONObject(0).getString("url");
                        }

                        // Link de la noticia
                        String newsUrl = "";
                        JSONObject linksObj = article.optJSONObject("links");
                        if (linksObj != null) {
                            JSONObject webObj = linksObj.optJSONObject("web");
                            if (webObj != null) {
                                newsUrl = webObj.optString("href");
                            }
                        }

                        listaNoticias.add(
                                new Noticia(headline, description, published, imageUrl, newsUrl)
                        );
                    }
                    //Agregamos la lista de noticias al RecyclerView
                    listAdapterNoticias = new ListAdapterNoticias(listaNoticias, requireContext());
                    recyclerView.setAdapter(listAdapterNoticias);
                    recyclerView.setVisibility(View.VISIBLE); //Mostramos las noticias obtenidas
                    linearLayoutProgressBar.setVisibility(View.GONE);
                    linearLayoutRecyclerView.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    linearLayoutProgressBar.setVisibility(View.GONE);
                    linearLayoutError.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                linearLayoutProgressBar.setVisibility(View.GONE);
                linearLayoutError.setVisibility(View.VISIBLE);
            }
        });
        queue.add(stringRequest);

        infoNoticias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog();
            }
        });
    }

    /**
     * Informamos al usuario de donde provienen las noticias
     */
    private void showInfoDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Fuente de las noticias")
                .setMessage("Estas noticias son obtenidas de ESPN vía su API oficial.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(request -> true);
        }
    }
}