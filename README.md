# ChatNow  

ChatNow es una aplicación de mensajería instantánea desarrollada para dispositivos Android. Su objetivo principal es proporcionar una plataforma sencilla y eficiente para que los usuarios puedan comunicarse entre sí mediante mensajes de texto e imágenes.  

## Funcionalidades Principales  

### 1. **Registro de Usuarios**  
- Permite a los usuarios crear una cuenta con su correo electrónico y contraseña.  
- La información básica del usuario se almacena en Firebase Realtime Database.  

### 2. **Inicio de Sesión**  
- Los usuarios pueden iniciar sesión utilizando su correo electrónico y contraseña.  
- Implementa Firebase Authentication para la gestión segura de credenciales.  

### 3. **Envío de Mensajes**  
- Soporta el envío de mensajes de texto en tiempo real entre usuarios.  
- Los mensajes se almacenan en Firebase Realtime Database y se sincronizan automáticamente en ambos dispositivos.  
- Los usuarios también pueden enviar imágenes.  

### 4. **Gestión de Perfil**  
- Los usuarios pueden editar su información personal, como nombre, apellidos, edad, profesión, domicilio y teléfono.  
- También pueden actualizar su imagen de perfil, almacenada en Firebase Storage.  

### 5. **Recuperación de Contraseña**  
- Los usuarios pueden recuperar su contraseña a través de un correo de restablecimiento enviado desde Firebase Authentication.  

## Tecnologías Utilizadas  

- **Android Studio**: Entorno de desarrollo integrado (IDE) utilizado para programar la aplicación en Kotlin.  
- **Firebase Authentication**: Gestión de registros, inicios de sesión y recuperación de contraseñas.  
- **Firebase Realtime Database**: Almacenamiento y sincronización de datos en tiempo real, como mensajes y perfiles de usuarios.  
- **Firebase Storage**: Almacenamiento de imágenes de perfil y otros archivos multimedia.  
- **Glide**: Biblioteca para la carga eficiente de imágenes en la interfaz de usuario.  


## Mejoras Futuras  

1. Implementar notificaciones push para informar a los usuarios cuando reciben nuevos mensajes.  
2. Ampliar la funcionalidad para permitir el envío de videos.  
3. Habilitar la creación de grupos para facilitar la comunicación entre varios usuarios en un solo chat.  

