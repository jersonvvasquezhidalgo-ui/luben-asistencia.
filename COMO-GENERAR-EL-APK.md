# Cómo generar el APK de Luben Asistencia

Este es el proyecto de la app de Android, listo para abrirse en **Android Studio**, que es gratuito. La app abre `https://rrhh.confiterialuben.com` en modo asistencia y permite usar la cámara para el QR, la ubicación, la subida de archivos y la descarga de PDF y Excel.

## Una sola vez: instalar Android Studio
1. Descargue Android Studio desde https://developer.android.com/studio e instálelo con las opciones por defecto (Next → Next → Finish).
2. Al abrirlo por primera vez elija **Standard** y deje que descargue el SDK. Tarda varios minutos.

## Generar el APK
1. Descomprima `LubenAsistencia-proyecto-android.zip` en una carpeta, por ejemplo `Documentos\LubenAsistencia`.
2. En Android Studio: **File → Open** → elija la carpeta `LubenAsistencia` (la que contiene `settings.gradle`) → **OK**. Si le pregunta si confía en el proyecto, elija **Trust Project**.
3. Espere a que termine la barra inferior ("Gradle sync"). La primera vez puede tardar 5 a 10 minutos.
4. Menú **Build → Generate Signed App Bundle / APK…** → elija **APK** → **Next**.
5. En *Key store path* pulse **Create new…**:
   - Elija dónde guardar la llave, por ejemplo `Documentos\luben-llave.jks`.
   - Escriba una contraseña y repítala.
   - En *Alias* escriba `luben`, repita la contraseña y en *First and Last Name* escriba `Confiteria Luben`.
   - **OK**.
   **Guarde ese archivo .jks y su contraseña en un lugar seguro.** Las actualizaciones futuras de la app deben firmarse con la misma llave. Si se pierde, los trabajadores tendrán que desinstalar la app y volver a instalarla.
6. **Next** → elija **release** → **Create**.
7. Al terminar, aparece un aviso abajo a la derecha. Pulse **locate**. El APK está en `app\release\app-release.apk`.
8. Cambie el nombre del archivo a `luben-asistencia-prueba.apk`.

## Publicarlo para los trabajadores
- Súbalo a su hosting en la carpeta `downloads` de `rrhh.confiterialuben.com` (Administrador de archivos → `downloads` → Cargar) y reemplace el archivo anterior.
- Los trabajadores lo descargan desde el celular con `https://rrhh.confiterialuben.com/downloads/luben-asistencia-prueba.apk` y lo instalan. Android pedirá permitir "instalar apps de fuentes desconocidas" para el navegador.

## Qué cambió respecto de la versión 1.2
- Versión 1.3.
- Ahora pide permiso de **ubicación**. La versión anterior no lo pedía, y la marcación por QR la necesita para verificar los 150 m de la tienda.
- Ícono cuadrado con el logo.
- Barra superior blanca.
