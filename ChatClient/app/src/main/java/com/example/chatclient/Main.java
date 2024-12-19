package com.example.chatclient;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends AppCompatActivity {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private EditText mensajeEntrada;
    private Button botonEnviar;
    private TextView vistaChat;
    private String nombreCliente;
    private String host = "10.0.2.2"; // Al ser un emulador el localhost en android es 10.0.2.2
    private int puerto = 12345;
    private Spinner spinner;
    private ArrayAdapter<String> usuariosAdapter; // Adaptador para manejar usuarios en el Spinner
    private List<String> usuariosConectados = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Recuperar los datos enviados desde la actividad anterior
        puerto = getIntent().getIntExtra("puerto", 12345);
        host = getIntent().getStringExtra("ip");
        nombreCliente = getIntent().getStringExtra("nombre");
        // Vincular elementos de la interfaz con sus IDs
        mensajeEntrada = findViewById(R.id.mensajeEntrada);
        botonEnviar = findViewById(R.id.botonEnviar);
        vistaChat = findViewById(R.id.vistaChat);
        spinner = findViewById(R.id.spinner);

        // Configurar el Spinner con un adaptador para mostrar los usuarios conectados
        usuariosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usuariosConectados);
        usuariosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(usuariosAdapter);
        // Configurar la barra de herramientas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Establecer el título del Toolbar con el nombre del cliente
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nombreCliente);
        }

        // Iniciar la conexión al servidor en un hilo separado
        new Thread(this::connectToServer).start();

        // Establece un listener para manejar el clic en el botón
        botonEnviar.setOnClickListener(v -> {
            String message = mensajeEntrada.getText().toString();
            String destinatario = spinner.getSelectedItem().toString(); // Usuario seleccionado

            if (!message.isEmpty()) {
                // Si se seleccionó un usuario específico, formatear el mensaje como privado
                if (!destinatario.equals("Todos")) {
                    message = "privado:" + destinatario + ":" + message; // Mensaje privado
                }
                sendMessage(message); // Enviar el mensaje al servidor
                mensajeEntrada.setText("");
            }
        });
    }

    // Metodo para conectar al servidor
    private void connectToServer() {
        try {
            // Crear el socket para la conexión
            socket = new Socket(host, puerto);
            writer = new PrintWriter(socket.getOutputStream(), true); // Flujo de salida
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flujo de entrada

            // Enviar el nombre del cliente al servidor
            writer.println(nombreCliente);

            // Hilo para leer mensajes desde el servidor
            new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) { // Bucle que escucha continuamente los mensajes enviados por el servidor
                        String finalMessage = message.trim(); // Elimina espacios

                        // Si el mensaje recibido comienza con "usuarios:", significa que es una lista de usuarios conectados
                        if (finalMessage.startsWith("usuarios:")) {
                            // Divide la lista de usuarios
                            String[] usuarios = finalMessage.substring(9).split(",");
                            runOnUiThread(() -> {
                                usuariosConectados.clear();
                                usuariosConectados.add("Todos"); // Añade la Opción para mensajes públicos
                                Arrays.stream(usuarios)
                                        .filter(usuario -> !usuario.equals(nombreCliente)) // Excluir al propio cliente
                                        .forEach(usuariosConectados::add); // Agrega los usuarios recibidos
                                usuariosAdapter.notifyDataSetChanged(); // Actualizar el Spinner
                            });
                        } else {
                            // Si no es una lista de usuarios, es un mensaje del chat
                            runOnUiThread(() -> {

                                if (finalMessage.contains("[Privado]:")) {
                                    // Mostrar el mensaje privado
                                    vistaChat.append("\n" + finalMessage);
                                } else {
                                    // Mostrar mensajes público
                                    vistaChat.append("\n" + finalMessage);
                                }
                            });

                        }

                    }

                } catch (Exception e) {
                    runOnUiThread(() -> mostrarVentanaEmergente("Conexión perdida", "Se ha perdido la conexión con el servidor."));
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> mostrarVentanaEmergente("Error", "No se pudo conectar al servidor."));
        }
    }

    // Metodo para mostrar un mensaje emergente al usuario
    private void mostrarVentanaEmergente(String titulo, String mensaje) {
        // Mostrar un mensaje emergente notificando al usuario
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)  // Evitar que se pueda cerrar sin interacción
                .setPositiveButton("Aceptar", (dialog, which) -> finish())  // Cerrar la actividad
                .show();
    }

    // Metodo para enviar/escribir un mensaje al servidor
    private void sendMessage(String message) {
        new Thread(() -> {
            try {
                // Enviar el mensaje al servidor
                writer.println(message);
                runOnUiThread(() -> {
                    if (message.startsWith("privado:")) {
                        // Si el mensaje es privado, se extraen las partes del mensaje
                        String[] partes = message.split(":", 3);
                        String destinatario = partes[1]; // Usuario al que se envió el mensaje
                        String contenido = partes[2]; // Contenido del mensaje
                        // Mostrar en el chat que el mensaje privado
                        vistaChat.append("\nEnviado: [Privado - " + destinatario + "]: " + contenido);
                    } else {
                        // Mostrar en el chat el mensaje publico
                        vistaChat.append("\nTú: " + message);
                    }
                });
            } catch (Exception e) {
                // Si ocurre un error al enviar el mensaje, notificarlo en el chat
                runOnUiThread(() -> vistaChat.append("\nError al enviar mensaje"));
            }
        }).start(); // Ejecutar el envío del mensaje en un hilo separado
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Cerrar el socket al destruir la actividad
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}