package com.adriangg.golarena;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.adriangg.golarena.Entity.Jugador;
import com.adriangg.golarena.ListAdapter.ListAdapterJugadores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class JugadoresFragment extends Fragment {

    RecyclerView recyclerViewJugadores;
    LinearLayout progressBarJugadores;
    LinearLayout avisoNoJuggadoresLayout;
    TextView txtMonedas;
    private String filtro;
    SearchView buscador;
    ImageButton btnOrdenar;
    List<Jugador> listaJugadores;
    private boolean cleanSearchView=false;
    // Executor para Room en background
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jugadores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewJugadores = view.findViewById(R.id.recyclerJugadores);

        recyclerViewJugadores.setLayoutManager(new LinearLayoutManager(requireContext()));
        progressBarJugadores = view.findViewById(R.id.menuProgressBarJugadoresLayout);
        txtMonedas = view.findViewById(R.id.txtMonedas);
        buscador = view.findViewById(R.id.search_view);
        btnOrdenar = view.findViewById(R.id.btnOrdenar);
        avisoNoJuggadoresLayout = view.findViewById(R.id.errorJugadoresLayout);
        recyclerViewJugadores.setVisibility(View.GONE);
        avisoNoJuggadoresLayout.setVisibility(View.GONE);
        progressBarJugadores.setVisibility(View.VISIBLE);
        int monedas = MainActivity.sharedPreferences.getInt("monedas",0);
        txtMonedas.setText(String.valueOf(monedas)); //Se muestra las monedas que tiene el usuario
        executor.execute(() -> { //Se obtiene los jugadores en otro hilo para no bloquear el hilo principal
            //Se obtiene los jugadores que tiene el usuario
            List<Jugador> jugadores = MainActivity.appDatabase.daoJugador().getJugadores();if (getActivity() != null) {
                listaJugadores=jugadores;
                getActivity().runOnUiThread(() -> {
                    progressBarJugadores.setVisibility(View.GONE);
                    if (jugadores.isEmpty()) {
                        avisoNoJuggadoresLayout.setVisibility(View.VISIBLE);
                    } else {
                        //Se añade al adapter y al recyclerview
                        ListAdapterJugadores adapterJugadores = new ListAdapterJugadores(jugadores, requireContext(), txtMonedas);
                        recyclerViewJugadores.setAdapter(adapterJugadores);
                        recyclerViewJugadores.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                boolean esCambiado = false;
                if(cleanSearchView){
                    return false;
                }
                if(s.isEmpty()){ //Si el buscador está vacío se vuelve a poner los videojuegos por defecto
                    recyclerViewJugadores.setVisibility(View.GONE);
                    progressBarJugadores.setVisibility(View.VISIBLE);
                    executor.execute(() -> { //Se obtiene los jugadores en otro hilo para no bloquear el hilo principal
                        //Se obtiene los jugadores que tiene el usuario
                        List<Jugador> jugadores = MainActivity.appDatabase.daoJugador().getJugadores();if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBarJugadores.setVisibility(View.GONE);
                                if (jugadores.isEmpty()) {
                                    avisoNoJuggadoresLayout.setVisibility(View.VISIBLE);
                                } else {
                                    //Se añade al adapter y al recyclerview
                                    ListAdapterJugadores adapterJugadores = new ListAdapterJugadores(jugadores, requireContext(), txtMonedas);
                                    recyclerViewJugadores.setAdapter(adapterJugadores);
                                    recyclerViewJugadores.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }else{ //En caso de que se escriba algo, se actualiza la variable filtro
                    filtro = s;
                    recyclerViewJugadores.setVisibility(View.GONE);
                    progressBarJugadores.setVisibility(View.VISIBLE);
                    executor.execute(() -> { //Se obtiene los jugadores en otro hilo para no bloquear el hilo principal
                        //Se obtiene los jugadores que tiene el usuario
                        List<Jugador> jugadores = MainActivity.appDatabase.daoJugador().getJugadoresPorNombre(filtro);if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBarJugadores.setVisibility(View.GONE);
                                if (jugadores.isEmpty()) {
                                    avisoNoJuggadoresLayout.setVisibility(View.VISIBLE);
                                } else {
                                    //Se añade al adapter y al recyclerview
                                    ListAdapterJugadores adapterJugadores = new ListAdapterJugadores(jugadores, requireContext(), txtMonedas);
                                    recyclerViewJugadores.setAdapter(adapterJugadores);
                                    recyclerViewJugadores.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
                return  esCambiado;
            }
        });
        btnOrdenar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(requireContext(), view);

                popup.getMenu().add("Ordenar por nombre");
                popup.getMenu().add("Ordenar por media");

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        cleanSearchView=true;
                        buscador.setQuery("", false);
                        buscador.clearFocus();
                        cleanSearchView=false;
                        recyclerViewJugadores.setVisibility(View.GONE);
                        progressBarJugadores.setVisibility(View.VISIBLE);
                        String titulo = item.getTitle().toString();

                        if (titulo.equals("Ordenar por nombre")) {

                            Collections.sort(listaJugadores, new Comparator<Jugador>() {
                                @Override
                                public int compare(Jugador j1, Jugador j2) {
                                    return j1.nombre.compareToIgnoreCase(j2.nombre);
                                }
                            });

                        } else if (titulo.equals("Ordenar por media")) {

                            Collections.sort(listaJugadores, new Comparator<Jugador>() {
                                @Override
                                public int compare(Jugador j1, Jugador j2) {
                                    return Integer.compare(j2.media, j1.media);
                                }
                            });

                        }
                        ListAdapterJugadores adapterJugadores = new ListAdapterJugadores(listaJugadores, requireContext(), txtMonedas);
                        recyclerViewJugadores.setAdapter(adapterJugadores);
                        progressBarJugadores.setVisibility(View.GONE);
                        recyclerViewJugadores.setVisibility(View.VISIBLE);
                        return true;
                    }
                });

                popup.show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdownNow();
    }
}