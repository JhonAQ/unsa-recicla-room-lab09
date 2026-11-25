# UNSA RECICLA — Laboratorio 09 (IDNP)

Aplicación Android individual para el laboratorio «Registro de datos locales con Room». Se basa en la propuesta del proyecto del curso UNSA RECICLA y no modifica AlertaUNI.

Repositorio de entrega: https://github.com/JhonAQ/unsa-recicla-room-lab09

## Funcionalidad

- Registra el retorno de 1 a 99 botellas PET; cada botella suma un EcoPunto.
- Persiste los movimientos en Room/SQLite y calcula el saldo desde el historial.
- Ofrece canjes de 5 puntos por 10 impresiones y 10 puntos por S/ 1 de descuento en comedor, de acuerdo con la propuesta recibida.
- Presenta los movimientos en una `LazyColumn` reactiva y permite eliminarlos.
- Usa una transacción al canjear y vuelve a consultar el saldo para evitar saldos negativos.

## Abrir y ejecutar

Abrir esta carpeta en Android Studio o ejecutar:

```bash
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. El proyecto requiere Android SDK 37 y Java 17. Las tres pruebas instrumentadas cubren inserción, consulta, actualización, eliminación, saldo, canje con saldo insuficiente y persistencia después de reabrir una base en disco.

## Estructura

- `app/src/main/java/pe/unsa/recicla/data/`: Entity, DAO y base de datos Room.
- `app/src/main/java/pe/unsa/recicla/MainActivity.kt`: interfaz Compose y operaciones.
- `app/src/androidTest/`: verificación instrumentada de Room.
- `img/capturas/`: capturas reales del teléfono y del emulador; el informe usa las capturas completas del teléfono.
- `snippets/`: extractos del código fuente incluidos en el informe.
- `report.pdf`: informe del laboratorio.

Este es un prototipo local. No integra las máquinas RVM, autenticación institucional, carné universitario ni servicios reales de canje.
