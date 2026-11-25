#import "lib.typ": unsa-report
#import "components/lab-section.typ": lab-section
#show: unsa-report.with(
  course_name: "Introducción al Desarrollo de Nuevas Plataformas (E)",
  lab_title: "Registro local de datos con Room · UNSA RECICLA",
  lab_number: "09",
  instructor_name: "Christian Alain Revilla Arroyo",
  members: ("Arias Quispe, Jhonatan David",),
  year: 2026,
  sem_code: "B",
  presentation_date: "25/11/2026",
)
#set par(justify: true)
#let snippet(path) = block(fill: rgb("#F1F3F4"), inset: 7pt, radius: 4pt, width: 100%, breakable: true, text(size: 7pt, raw(read(path), lang: "kotlin", block: true)))

#lab-section(title: "I. PROPÓSITO Y CONTEXTO")[
El laboratorio 09 de IDNP trata sobre *persistencia local en Android con Room*: definir una entidad con clave primaria y atributos, crear un DAO para guardar y consultar datos, construir una base Room, mostrar los registros en una interfaz y comprobar que continúan allí después de reiniciar la aplicación. La guía exige además una operación de actualización o eliminación, evidencias visuales y respuestas conceptuales.

Se aplicó este objetivo a *UNSA RECICLA*, proyecto del curso descrito en la propuesta RVM proporcionada. En el prototipo, cada botella PET retornada produce un EcoPunto. Los movimientos de retorno y canje se guardan localmente; el saldo resulta de sumar sus puntos. La propuesta entregada es documentación, no código fuente Android. Por ello se implementó una app independiente para este laboratorio. No se presenta como integración con una máquina RVM real.
]

#lab-section(title: "II. DISEÑO E IMPLEMENTACIÓN")[
*Flujo de datos.* El usuario registra botellas en Compose. La pantalla llama al repositorio, que valida la operación y utiliza el DAO. Room escribe en SQLite; los flujos del DAO emiten las filas y el saldo actualizados. Compose los observa y vuelve a dibujar la tarjeta de saldo y el historial.

#align(center)[#box(fill: rgb("#EEF5F0"), inset: 9pt, radius: 4pt)[Interfaz Compose → Repositorio → DAO → Room/SQLite → Flow → Interfaz Compose]]

*Entidad.* Cada fila identifica un movimiento y guarda tipo, descripción, cambio de puntos y fecha. El identificador se genera automáticamente; los puntos positivos representan retornos y los negativos, canjes. Fragmento de EcoTransaction.kt:

#snippet("snippets/entity.kt")

*DAO y actualización reactiva.* La consulta ordena los movimientos del más reciente al más antiguo. Otra consulta suma sus puntos; al cambiar una fila, Room emite un saldo nuevo. También se definieron inserción, actualización y eliminación. El siguiente fragmento muestra las dos consultas que alimentan la pantalla; el resto del DAO está en el código fuente adjunto.

#snippet("snippets/dao.kt")

*Reglas de operación.* El formulario admite entre 1 y 99 botellas. El repositorio registra un retorno con la misma cantidad de puntos. Los canjes usan una transacción Room: se consulta el saldo y solo se inserta el débito si alcanza para el beneficio. Esta verificación ocurre en la misma transacción para evitar que dos canjes simultáneos consuman los mismos puntos. La interfaz ofrece, según la propuesta, diez impresiones por cinco puntos y S/ 1 de descuento en comedor por diez puntos. La clase EcoRepository.kt contiene estas reglas completas.

*Pantalla.* Los flujos se convierten en estado de Compose. La lista muestra cada movimiento con descripción, fecha, puntos y botón de eliminación. Las escrituras se ejecutan en Dispatchers.IO para no bloquear la interfaz. El siguiente fragmento vincula las consultas reactivas con la pantalla y valida el número ingresado:

#snippet("snippets/ui.kt")
]

#lab-section(title: "III. PRUEBAS Y RESULTADOS")[
Se ejecutó assembleDebug y luego connectedDebugAndroidTest en un emulador Android API 37. Las *tres pruebas instrumentadas terminaron correctamente*:

#table(
  columns: (1.25fr, 2.6fr, 0.7fr),
  inset: 5pt,
  stroke: 0.4pt + rgb("#B7C6BF"),
  [*Prueba*], [*Qué comprueba*], [*Resultado*],
  [CRUD y saldo], [Inserta, consulta, actualiza 3 → 4 puntos y elimina; termina en saldo 0.], [Pasó],
  [Canje], [Rechaza canje sin saldo; registra 7 puntos, canjea 5 y obtiene saldo 2.], [Pasó],
  [Reapertura], [Cierra una base Room en disco, la reabre y recupera saldo y movimiento.], [Pasó],
)

La prueba de reapertura verifica la persistencia en una base con nombre. También se recorrió el flujo en un teléfono Android físico conectado por USB: se registró una botella, se cerró el proceso, se abrió nuevamente y se observó el mismo saldo y movimiento. Finalmente, se eliminó la fila y la lista quedó vacía con saldo cero. Las figuras muestran *capturas completas de pantalla*, sin recortar sus bordes; la pantalla de historial corresponde a la vista desplazada hacia abajo dentro de la aplicación.

*Correspondencia con la rúbrica.* El modelo y la base se observan en EcoTransaction.kt y AppDatabase.kt; el DAO y el acceso asíncrono, en EcoTransactionDao.kt, EcoRepository.kt y MainActivity.kt. El formulario y LazyColumn se comprueban en las capturas de registro e historial. La operación adicional y la persistencia se comprueban con las pruebas, la reapertura en el teléfono y la captura posterior a la eliminación. El flujo UI–Room–UI se explica en la sección II. El código actualizado está disponible en #link("https://github.com/JhonAQ/unsa-recicla-room-lab09")[GitHub]. La calificación corresponde al docente.

#figure(grid(columns: (1fr, 1fr), gutter: 10pt, align(center, image("img/capturas/celular-pantalla-inicial.png", width: 75%)), align(center, image("img/capturas/celular-retorno-historial.png", width: 75%))), caption: [Teléfono físico: pantalla inicial con saldo cero (izquierda) y vista desplazada al historial después de registrar una botella, con saldo uno y movimiento RETORNO (derecha).])
#figure(grid(columns: (1fr, 1fr), gutter: 10pt, align(center, image("img/capturas/celular-reinicio-superior.png", width: 75%)), align(center, image("img/capturas/celular-eliminacion.png", width: 75%))), caption: [Teléfono físico: saldo uno después de cerrar y reabrir la app (izquierda); después de eliminar el movimiento, saldo cero y lista vacía (derecha).])

La evidencia permite afirmar que Room conserva los movimientos dentro del almacenamiento local de la app y que la interfaz reacciona a los cambios. El canje se verificó mediante prueba instrumentada; las capturas del teléfono corresponden al registro, la reapertura y la eliminación. No se probó un canje institucional efectivo ni una máquina RVM.
]

#lab-section(title: "IV. CUESTIONARIO")[
*1. ¿Qué ventaja aporta Room frente a usar SQLite directamente?* Room conserva SQLite como almacenamiento, pero aporta entidades y consultas tipadas, comprobación de SQL en compilación y conversión de filas a objetos. Esto reduce código de conexión y lectura escrito a mano. En este caso, Flow facilita actualizar automáticamente el saldo y el historial.

*2. ¿Qué responsabilidad tiene el DAO?* El DAO define la forma de leer y modificar datos: inserción, consulta ordenada, saldo agregado, actualización y eliminación. Así, la pantalla no contiene sentencias SQL ni conoce los detalles de las tablas.

*3. ¿Por qué separar el acceso a datos de la interfaz?* La separación permite que Compose se ocupe de presentar el estado y recibir acciones. El repositorio concentra reglas como la validación del canje y el DAO concentra las consultas. Con ello las operaciones se prueban sin depender de la apariencia de la pantalla y se evitan accesos a la base en el hilo de dibujo.
]

#lab-section(title: "V. CONCLUSIONES Y ALCANCE")[
Se completaron los componentes solicitados por la guía: entidad Room con clave primaria y más de tres atributos, DAO con operaciones de escritura y consulta, base local, formulario, LazyColumn, eliminación y evidencia de persistencia. La compilación y las tres pruebas instrumentadas pasaron en el emulador. El recorrido manual confirmó registro, reapertura y eliminación visibles en la interfaz.

El prototipo es local e individual. No incorpora identificación del estudiante, comunicación con hardware RVM ni emisión real de beneficios; esos componentes pertenecen al proyecto completo. Si se integra al sistema del curso, las reglas de EcoPuntos y canjes deberán validarse con el equipo y asociarse a usuarios autenticados.
]

#lab-section(title: "REFERENCIAS")[
Guía de Práctica de Laboratorio 09, “Registro de datos locales con Room”, IDNP, 2026-B.

Rúbrica del Laboratorio 09, IDNP, 2026-B.

Propuesta Proyecto RVM UNSA Equipo Sistemas, “UNSA RECICLA”, septiembre de 2026.

Android Developers. “Save data in a local database using Room”. https://developer.android.com/training/data-storage/room?hl=en
]
