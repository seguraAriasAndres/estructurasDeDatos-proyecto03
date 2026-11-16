# Social UNA

Prototipo de red social de escritorio para la comunidad estudiantil de la Universidad Nacional de Costa Rica.

## Funciones

- Registro e inicio de sesión
- Búsqueda de estudiantes y conexión con otras cuentas
- Publicación de mensajes
- Muro con publicaciones propias y de las conexiones
- Guardado local de cuentas, conexiones y publicaciones en archivos JSON

## Requisitos

- Java JDK 17 o posterior
- Conexión a internet la primera vez que se ejecuta, para descargar las dependencias de Maven

## Datos de demostración

Las tres cuentas de ejemplo usan la contraseña `socialuna2026`.

| Usuario | Nombre |
| --- | --- |
| `ana.ruiz` | Ana Ruiz |
| `luis.vargas` | Luis Vargas |
| `sofia.mora` | Sofía Mora |

Los archivos de ejemplo están en `sample-data/`. Para cargarlos, ejecuta desde la carpeta raíz del proyecto:

    New-Item -ItemType Directory -Force .\data
    Copy-Item .\sample-data\*.json .\data\

**Advertencia:** copiar los ejemplos reemplaza archivos con los mismos nombres dentro de `data/`. Hazlo en una copia limpia del proyecto o respalda primero tus datos.

## Almacenamiento

La aplicación guarda las cuentas, conexiones y publicaciones en archivos JSON dentro de `data/`, relativo a la carpeta desde la que se ejecuta.

## Tecnologías

- Java 17
- JavaFX
- Maven
- Gson
- Archivos JSON

Proyecto realizado para el curso de Estructuras de Datos.
