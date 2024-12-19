package com.example.chatclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.Socket;

public class Config extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Vincular elementos de la interfaz con sus IDs
        EditText entradaIp = findViewById(R.id.entradaIp);
        EditText entradaPuerto = findViewById(R.id.entradaPuerto);
        EditText entradaNombre = findViewById(R.id.entradaNombre);
        Button botonConectar = findViewById(R.id.botonConectar);

        // Establece un listener para manejar el clic en el botón
        botonConectar.setOnClickListener(v -> {
            // Obtiene los valores de los campos de texto o asigna valores predeterminados si están vacíos
            String ipText = entradaIp.getText().toString().isEmpty() ? "10.0.2.2" : entradaIp.getText().toString();
            String puertoText = entradaPuerto.getText().toString().isEmpty() ? "12345" : entradaPuerto.getText().toString();
            String nombreText = entradaNombre.getText().toString();

            // Verifica que el campo de nombre no esté vacío
            if (!nombreText.isEmpty()) {
                try {
                    int puerto = Integer.parseInt(puertoText);
                    // Valida que el puerto esté en el rango permitido (1-65535)
                    if (puerto >= 1 && puerto <= 65535) {
                        // Llama al metodo para realizar la conexión en un hilo separado
                        conectarEnHilo(ipText, puerto, nombreText);
                    } else {
                        // Muestra un mensaje si el puerto está fuera del rango permitido
                        Toast.makeText(this, "Ingrese un puerto válido (1-65535)", Toast.LENGTH_SHORT).show();
                    }
                } catch (
                        NumberFormatException e) { // Maneja la excepción si el puerto no es un número válido
                    Toast.makeText(this, "Ingrese un número válido para el puerto", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Muestra un mensaje si el campo de nombre está vacío
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metodo para realizar la conexión en un hilo separado
    private void conectarEnHilo(String host, int puerto, String nombre) {
        new Thread(() -> {
            try {
                // Intenta establecer una conexión con el servidor
                Socket socket = new Socket(host, puerto);

                // Si la conexión es exitosa, cambia a la actividad principal
                runOnUiThread(() -> {
                    // Muestra un mensaje de éxito
                    Toast.makeText(getApplicationContext(), "Conexión exitosa", Toast.LENGTH_SHORT).show();

                    // Crea un intent para pasar a la actividad principal y enviar datos
                    Intent intent = new Intent(Config.this, Main.class);
                    intent.putExtra("ip", host);
                    intent.putExtra("puerto", puerto);
                    intent.putExtra("nombre", nombre);
                    startActivity(intent);
                    finish(); // Finaliza la actividad actual
                });
            } catch (Exception e) {// Maneja excepciones en caso de error de conexión
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start(); // Inicia el hilo
    }
}

