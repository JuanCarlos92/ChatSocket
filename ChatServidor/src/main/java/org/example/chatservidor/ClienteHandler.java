package org.example.chatservidor;

import org.example.chatservidor.controller.LogsServidorController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteHandler implements Runnable {

    private Socket socket;
    private LogsServidorController servidor;
    private PrintWriter writer;
    private String nombre;

    // Constructor para inicializar el manejador del cliente
    public ClienteHandler(Socket socket, LogsServidorController servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }


    @Override
    public void run() {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flujo de entrada
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) { // Flujo de salida

            this.writer = writer;

            // Leemos el nombre del cliente al inicio de la conexión
            nombre = read.readLine();
            servidor.agregarCliente(this); // Registramos al cliente en el servidor

            // Mientras mensaje no sea nulo... procesa el mensaje
            String mensaje;
            while ((mensaje = read.readLine()) != null) {
                servidor.procesarMensaje(mensaje, this);
            }

        } catch (IOException e) {
            servidor.log("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            cerrarConexion(); // Llamar a cerrar la conexión correctamente
        }
    }

    // Metodo para enviar un mensaje al cliente
    public void enviarMensaje(String mensaje) {
        if (writer != null) { // Verifica flujo de salida no sea null
            writer.println(mensaje); // Envia el mensaje al cliente
        }
    }

    // Metodo para cerrar la conexión con el cliente
    public void cerrarConexion() {
        try {
            if (!socket.isClosed()) { // Verifica si el socket está abierto
                socket.close(); // Cierra el socket
            }
        } catch (IOException e) {
            servidor.log("Error al cerrar la conexión: " + e.getMessage());
        }

        servidor.removerCliente(this); // Remover al cliente de la lista de clientes activos
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNombre() {
        return nombre;
    }
}
