package com.adriangg.golarena;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.adriangg.golarena.ListAdapter.ListAdapterTienda;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StoreFragment extends Fragment {

    RecyclerView recyclerViewTienda;
    LinearLayout layoutMonedas;
    LinearLayout progressBarLayout;
    LinearLayout errorTiendaLayout;

    ImageView btnInfoTienda;
    ImageButton btnSobres;
    TextView txtMonedas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewTienda = view.findViewById(R.id.recyclerTiendaJugadores);
        recyclerViewTienda.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        layoutMonedas = view.findViewById(R.id.layoutMonedasTienda);
        txtMonedas = view.findViewById(R.id.txtMonedasTienda);
        progressBarLayout = view.findViewById(R.id.menuProgressBarTiendaLayout);
        errorTiendaLayout = view.findViewById(R.id.errorTiendaLayout);
        btnInfoTienda = view.findViewById(R.id.btnInfoTienda);
        btnSobres = view.findViewById(R.id.btnImagen);
        recyclerViewTienda.setVisibility(View.GONE);
        layoutMonedas.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
        int monedas = MainActivity.sharedPreferences.getInt("monedas", 0);
        txtMonedas.setText(String.valueOf(monedas));
        MainActivity.appDatabase.daoTienda().getJugadores().observe(getViewLifecycleOwner(), jugadorTiendas -> {
            progressBarLayout.setVisibility(View.GONE);
            if (jugadorTiendas != null && !jugadorTiendas.isEmpty()) {
                recyclerViewTienda.setVisibility(View.VISIBLE);
                layoutMonedas.setVisibility(View.VISIBLE);
                recyclerViewTienda.setAdapter(
                        new ListAdapterTienda(jugadorTiendas, requireContext(), txtMonedas)
                );
            } else {
                errorTiendaLayout.setVisibility(View.VISIBLE);
            }
        });
        //En caso de hacer clic en el ícono del sobre, nos vamos a la Activity del sobre
        btnSobres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), SobreActivity.class);
                startActivity(intent);
            }
        });

        btnInfoTienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog();
            }
        });
    }

    /**
     * Mostramos la información de la tienda
     */
    private void showInfoDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información Tienda")
                .setMessage("La tienda se reinicia cada 24 horas.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}