# Proyecto Chat Socket (Servidor JavaFX y Cliente Android)

Este proyecto es una aplicación de chat en tiempo real implementada utilizando **sockets**. La arquitectura consiste en un servidor desarrollado en Java con una interfaz en JavaFX y un cliente en Android, ambos comunicándose mediante una conexión socket.

## Características

- **Servidor en JavaFX**:
  - Manejo de múltiples clientes simultáneamente.
  - Interfaz gráfica para monitorear las conexiones activas.
  - Logs en tiempo real.
- **Cliente Android**:
  - Interfaz sencilla y amigable para enviar y recibir mensajes.
  - Posibilidad de enviar mensaje a todos los clientes o mediante mensajes privados.
  - Diseño basado en **Material Design** para una mejor experiencia de usuario.

## Requisitos

### Servidor
- Version de java utilizada Java 23.
- IDE recomendado: IntelliJ IDEA.

### Cliente
- Android Studio.
- Android SDK 35 o superior.

## Instalación

### Servidor
1. Clona este repositorio:
   ```bash
   git clone https://github.com/usuario/chat-socket-java.git
2. Importa el proyecto en tu IDE.
3. Configura las dependencias de JavaFX si es necesario.
4. Ejecuta la clase principal del servidor: ServidorApp.

### Cliente
1. Clona este repositorio en tu máquina
   ```bash
   git clone https://github.com/usuario/chat-socket-java.git
2. Abre el proyecto en Android Studio.
3. Ejecuta la aplicación en un emulador o dispositivo físico.
   
## Uso
1. Inicia el servidor desde tu computadora.
2. Abre la aplicación cliente en Android y conecta con el servidor utilizando la IP y el puerto deseado.
3. Envía mensajes entre dispositivos conectados al mismo servidor.
