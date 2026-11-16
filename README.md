# Social UNA

Una red social para la comunidad estudiantil de la Universidad Nacional de Costa Rica. El proyecto permite crear cuentas, encontrar estudiantes, establecer conexiones y compartir publicaciones.

## Funcionalidades

- Registro e inicio de sesión de estudiantes.
- Selección de carrera y campus durante el registro, con opciones filtradas según la oferta académica de 2026.
- Búsqueda de estudiantes por nombre o usuario. Si el campo queda vacío, se muestran las demás cuentas registradas.
- Agregar y eliminar conexiones.
- Muro con publicaciones propias y de las conexiones.
- Tema claro y tema oscuro.
- Guardado de cuentas, conexiones y publicaciones en archivos JSON.

## Tecnologías

- Java 17
- JavaFX
- Maven
- Gson
- Persistencia local con archivos JSON

## Cómo ejecutar el proyecto

Desde intelliJ

## Cuentas de ejemplo

Las cuentas de demostración usan esta contraseña:

**Contraseña:** `socialuna2026`

| Usuario | Nombre |
|---|---|
| `ana.ruiz` | Ana Ruiz |
| `luis.vargas` | Luis Vargas |
| `sofia.mora` | Sofía Mora |

Para cargar los datos de ejemplo en una instalación nueva, copia los archivos de `sample-data/` a `data/`:

```powershell
New-Item -ItemType Directory -Force data
Copy-Item sample-data\*.json data\
```

La aplicación guarda sus datos en `data/`, dentro de la carpeta del proyecto. Copiar los ejemplos puede reemplazar archivos existentes con los mismos nombres; hazlo únicamente para preparar una instalación de demostración.

## Estructuras de datos

El proyecto utiliza colecciones en memoria para trabajar con las cuentas y publicaciones, y archivos JSON para conservar los datos entre ejecuciones. Las conexiones entre estudiantes se administran mediante un grafo con listas de adyacencia.

## Interfaz

La interfaz de usuario fue realizada con JavaFX y usando una plantilla '.css' para el diseño. El tema se puede cambiar desde las pantallas principales.

---

Proyecto académico para el curso de **Estructuras de Datos**.
