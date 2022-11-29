package com.cieep.a05_ejercicio_lista_compra;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.cieep.a05_ejercicio_lista_compra.adapters.ProductosAdapter;
import com.cieep.a05_ejercicio_lista_compra.modelos.Producto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;



import com.cieep.a05_ejercicio_lista_compra.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private ArrayList<Producto> productosList;

    // - RECYCLER -
    private ProductosAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    //Ej8
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Constantes.DATOS, MODE_PRIVATE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        productosList = new ArrayList<>();



        int columnas;
        //Horizontal -> 2
        //Vertical -> 1
        //DESDE LAS CONFIGURACIONES DE LA ACTIVIDAD -> orientation // PORTRAIT(vertical) / LANDSCAPE(horizontal)
        columnas = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;

        adapter = new ProductosAdapter(productosList, R.layout.producto_model_card, this);
        layoutManager = new GridLayoutManager(this, columnas);
        binding.contentMain.contenedor.setAdapter(adapter);
        binding.contentMain.contenedor.setLayoutManager(layoutManager);

        cargarDatosJSON();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creteProducto().show();
            }
        });
    }

    //Ej8
    private void guardarDatosJSON() {
        String productosStr = new Gson().toJson(productosList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.PRODUCTOSCOMPRA, productosStr);
        editor.apply();
    }

    //Ej8
    private void cargarDatosJSON(){
        if(sharedPreferences.contains(Constantes.PRODUCTOSCOMPRA) && !sharedPreferences.getString(Constantes.PRODUCTOSCOMPRA, "").isEmpty()){
            String productosStr = sharedPreferences.getString(Constantes.PRODUCTOSCOMPRA, "[]");
            Type tipo = new TypeToken< ArrayList<Producto> >(){}.getType();
            ArrayList<Producto> temp = new Gson().fromJson(productosStr, tipo);
            productosList.clear();
            productosList.addAll(temp);
            adapter.notifyItemRangeInserted(0,temp.size());
        }
    }


    private AlertDialog creteProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.alert_title_crear));
        builder.setCancelable(false);

        View productoAlertView = LayoutInflater.from(this).inflate(R.layout.producto_model_alert, null);
        builder.setView(productoAlertView);

        EditText txtNombre = productoAlertView.findViewById(R.id.txtNombreProductoAlert);
        EditText txtCantidad = productoAlertView.findViewById(R.id.txtCantidadProductoAlert);
        EditText txtPrecio = productoAlertView.findViewById(R.id.txtPrecioProductoAlert);
        TextView lblTotal = productoAlertView.findViewById(R.id.lblTotalProductoAlert);

        TextWatcher textWatcher = new TextWatcher() {

            /**
             * Al modificar un cuadro de texto
             * @param charSequence -> envia el contenido que había antes del cambio
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /**
             * al modificar un cuadro de texto
             * @param charSequence -> Envia el texto actual despues de la modificación
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /**
             * se dispara al terminar  la modificación
             * @param editable -> envia el contenido final del cuadro de texto
             */
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int cantidad = Integer.parseInt(txtCantidad.getText().toString());
                    float precio = Float.parseFloat(txtPrecio.getText().toString());
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
                    lblTotal.setText(numberFormat.format(cantidad*precio));
                }
                catch (NumberFormatException ex) {}

            }
        };

        txtCantidad.addTextChangedListener(textWatcher);
        txtPrecio.addTextChangedListener(textWatcher);

        builder.setNegativeButton(getString(R.string.btn_alert_cancel), null);
        builder.setPositiveButton(getString(R.string.btn_alert_crear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!txtNombre.getText().toString().isEmpty()
                    && !txtCantidad.getText().toString().isEmpty()
                    && !txtPrecio.getText().toString().isEmpty() ) {
                    Producto producto = new Producto(
                                        txtNombre.getText().toString(),
                                        Integer.parseInt(txtCantidad.getText().toString()),
                                        Float.parseFloat(txtPrecio.getText().toString())
                    );
                    productosList.add(0, producto);
                    adapter.notifyItemInserted(0);
                    guardarDatosJSON();
                }
                else {
                    Toast.makeText(MainActivity.this, "FALTAN DATOS", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return builder.create();
    }




    /**
     * Se dispara antes de que se elimine la actividad
     * @param outState -> guardo los datos
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("LISTA", productosList);
    }

    /**
     * Se dispara despues de crear la actividad dfe nuevo
     * @param savedInstanceState -> recupero los datos
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Producto> temp = (ArrayList<Producto>) savedInstanceState.getSerializable("LISTA");
        productosList.addAll(temp);
        adapter.notifyItemRangeInserted(0, productosList.size());
    }

}