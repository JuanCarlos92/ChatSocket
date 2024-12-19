package org.example.chatservidor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

public class ConfiguracionPuertoController {
    @FXML
    private TextField portTextField; // Campo de texto para el número de puerto
    private static final int DEFAULT_PORT = 12345; // Puerto predeterminado

    // Metodo que se ejecuta cuando el usuario presiona el botón para iniciar el servidor
    @FXML
    private void startServer() {
        String input = portTextField.getText().trim(); // Obtiene el texto, eliminando espacios
        int port;

        // Si el usuario no ingresa un puerto, usa el valor predeterminado
        if (input.isEmpty()) {
            port = DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(input);
                // Verifica el puerto
                if (port <= 0 || port > 65535) {
                    throw new NumberFormatException("Puerto fuera de rango");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Puerto no válido. Use un número entre 1 y 65535.");
                return; // Termina el metodo para evitar continuar con un puerto inválido
            }
        }

        try { // Intenta cargar la ventana de logs del servidor
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chatservidor/logs_servidor.fxml"));
            Scene scene = new Scene(loader.load()); // Crea una nueva escena
            LogsServidorController controller = loader.getController(); // Obtiene el controlador de la ventana de LogsServidorController
            controller.iniciarServidor(port); // Llama al metodo que inicia el servidor con el puerto

            // Cambia la ventana actual por la ventana de logs del servidor
            Stage stage = (Stage) portTextField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Logs del Servidor");
        } catch (IOException e) {
            showAlert("Error", "No se pudo cargar la ventana de logs del servidor.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}