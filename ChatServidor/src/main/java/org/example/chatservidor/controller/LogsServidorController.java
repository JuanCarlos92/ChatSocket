package org.example.chatservidor.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.example.chatservidor.ClienteHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class LogsServidorController {

    @FXML
    private TextArea logTextArea; // Área de texto en la interfaz donde se mostrarán los logs del servidor

    private ServerSocket serverSocket; // Socket escuchar conexiones entrantes
    private boolean bandera;
    private Set<ClienteHandler> clientes; // Lista de clientes

    // Metodo para iniciar el servidor en un puerto especificado
    public void iniciarServidor(int port) {
        clientes = new HashSet<>(); // Inicializa la colección
        bandera = true;

        // Inicia un nuevo hilo para manejar el servidor
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port); // Crea el servidor en el puerto dado
                log("Servidor iniciado en el puerto [" + port + "]"); // 1º Log (Puerto)

                //Si bandera = true... (bucle)
                while (bandera) {
                    var socket = serverSocket.accept(); // Espera conexiones
                    var cliente = new ClienteHandler(socket, this); // Crea un manejador para el cliente conectado
                    new Thread(cliente).start(); // Inicia un nuevo hilo para manejar al cliente
                }
            } catch (IOException e) {
                log("Error en el servidor: " + e.getMessage());
            } finally {
                detenerServidor(); // Asegura detener el servidor
            }
        }).start(); //Iniciar hilo
    }

    // Metodo para detener el servidor
    public void detenerServidor() {
        bandera = false; //Cambia bandera a false
        try {
            // Cerrar todas las conexiones activas
            for (ClienteHandler cliente : clientes) {
                cliente.cerrarConexion();
            }
            clientes.clear(); //Limpia la lista
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); //Cierra el socket
            }
            log("Servidor detenido.");
        } catch (IOException e) {
            log("Error al detener el servidor: " + e.getMessage());
        }
    }

    // Metodo para agregar mensajes al área de texto (logs)
    public void log(String message) {
        // Usamos Platform.runLater para actualizar la interfaz desde un hilo secundario
        Platform.runLater(() -> logTextArea.appendText(message + "\n"));
    }

    // Metodo para enviar la lista actualizada de usuarios conectados
    private synchronized void enviarListaUsuarios() {
        StringBuilder usuarios = new StringBuilder("usuarios:"); // Inicia el mensaje con "usuarios:"
        for (ClienteHandler cliente : clientes) {
            if (cliente.getNombre() != null) {
                usuarios.append(cliente.getNombre()).append(","); // Agrega cada usuario al mensaje
            }
        }
        // Remover la última coma
        if (usuarios.length() > 9) {
            usuarios.setLength(usuarios.length() - 1);
        }
        broadcast(usuarios.toString(), null); // Envía la lista de usuarios a todos los clientes
    }

    // Metodo sincronizado para agregar cliente
    public synchronized void agregarCliente(ClienteHandler cliente) {
        if (cliente.getNombre() != null) {  //Si nombre no es null...
            if (clientes.add(cliente)) { //añade cliente a la lista
                broadcast("Cliente conectado: " + cliente.getNombre(), null);  // Notifica la conexión a todos
                enviarListaUsuarios(); // Enviar lista actualizada
            }
        }
    }

    // Metodo sincronizado para eliminar clientes
    public synchronized void removerCliente(ClienteHandler cliente) {
        if (cliente.getNombre() != null) { //Si nombre no es null...
            if (clientes.remove(cliente)) { //elimina cliente de la lista
                broadcast("Cliente desconectado: " + cliente.getNombre(), null); // Notifica la desconexión a todos
                enviarListaUsuarios(); // Enviar lista actualizada
            }
        }
    }

    // Metodo para procesar mensajes recibidos de un cliente
    public synchronized void procesarMensaje(String mensaje, ClienteHandler remitente) {
        if (mensaje.startsWith("privado:")) {
            // Procesar mensaje privado (formato: privado:destinatario:contenido)
            String[] partes = mensaje.split(":", 3);
            if (partes.length == 3) {
                String destinatario = partes[1].trim(); // Nombre del destinatario
                String contenido = partes[2].trim();    // Contenido del mensaje

                boolean enviado = false;

                // Busca al destinatario en la lista de clientes y envía el mensaje privado
                for (ClienteHandler cliente : clientes) {
                    if (cliente.getNombre().equals(destinatario)) {
                        // Formato: cliente [Privado]: mensaje
                        cliente.enviarMensaje(remitente.getNombre() + " [Privado]: " + contenido);
                        enviado = true; // Marca como enviado
                        break;
                    }
                }

                //si enviado = false
                if (!enviado) {
                    remitente.enviarMensaje("[servidor] Usuario no encontrado: " + destinatario);
                }
            } else { // Notificar al remitente que el formato es incorrecto
                remitente.enviarMensaje("[servidor] Formato de mensaje privado incorrecto. Use: privado:destinatario:contenido");
            }
        } else {
            // Mensaje público (se envía a todos)
            String mensajePublico = remitente.getNombre() + ": " + mensaje;
            broadcast(mensajePublico, remitente); // Llama al metodo para difundir mensajes
        }
    }

    // Metodo para enviar mensajes a todos los clientes
    public synchronized void broadcast(String message, ClienteHandler remitente) {
        for (ClienteHandler cliente : clientes) {
            // No enviar mensajes al remitente si es especificado
            if (cliente != remitente) {
                cliente.enviarMensaje(message);
            }
        }
        if (remitente == null) {
            log(message);
        }
    }

}