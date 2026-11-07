# QuimiMatch — laboratorio molecular bajo ataque

Esta version conserva los 24 niveles y los efectos de impacto ya probados. La fase posterior al runner ahora es MolecularLabScreen, en lugar del tablero match-3. El tablero anterior queda en el codigo, sin formar parte del recorrido normal.

## Controles
- Selecciona uno de los atomos de la barra inferior.
- DISPARAR x3 o tocar el reactor envia un pulso con hasta tres atomos. Cada disparo consume un pulso. Solo hay un disparo propio en vuelo.
- Los contadores superiores muestran exactamente cuales atomos faltan.
- Una carga roja tarda unos dos segundos en llegar al reactor. ESCUDO dura 1,25 segundos y recarga en 3 segundos: activalo cuando la carga se acerque.
- Carga recibida: -20 estabilidad. Atomo innecesario: -10 estabilidad.
- Completar los objetivos neutraliza al Nucleo Inestable con una onda expansiva y celebracion.
- 80% de estabilidad al ganar garantiza 3 estrellas; 50% garantiza 2.
- PAUSA detiene los ataques. Al volver de otra aplicacion queda pausado.

Los atomos del runner aportan hasta una cuarta parte de cada objetivo. No se requiere completar el runner para disponer de todos los tipos de atomo en el laboratorio. Los objetivos mantienen la configuracion existente de los niveles; no se ha hecho una revision cientifica de todas las formulas y cantidades heredadas.

## Archivos de esta actualizacion
core/src/main/java/com/quimimatch/screens/MolecularLabScreen.java (nuevo)
core/src/main/java/com/quimimatch/screens/RunnerScreen.java (transicion y atomos del runner)
core/src/main/java/com/quimimatch/screens/DefeatScreen.java (reintento desde runner y titulo)

La perdida de vida al fracasar se aplica una sola vez, al entrar a la pantalla de derrota.

## Verificacion
- Analisis sintactico Java sin errores.
- Prueba ejecutada de GameSession y LevelConfig para los 24 niveles: objetivos alcanzables con pulsos de tres atomos dentro de los movimientos, sin exceder cantidades y sin que la bonificacion del runner salte el combate. Para aislar esta logica sin LibGDX se uso un sustituto minimo de Color.
- No se pudo compilar la app ni generar APK porque este entorno no puede descargar Gradle. La sintaxis y la prueba de objetivos no sustituyen la compilacion ni la prueba visual en Android.

## Prueba en tu celular
1. Deten la ejecucion anterior. Respalda tu proyecto actual.
2. Copia los tres archivos indicados conservando sus rutas, o abre este proyecto extraido como carpeta independiente. Conserva tu local.properties y tu carpeta .git.
3. Ejecuta android en Android Studio. No desinstales la app.
4. Entra a un nivel, termina el runner y pulsa ENTRAR AL LABORATORIO.
5. Selecciona un atomo necesario y dispara: comprobar proyectil, contador y progreso en el reactor.
6. Prueba ESCUDO cerca de la llegada de la carga roja y espera su recarga.
7. Deja pasar una carga: comprobar vibracion y estabilidad -20.
8. Dispara un atomo sobrante: comprobar estabilidad -10.
9. Comprueba PAUSA, victoria, derrota y reintento del mismo nivel.

## Actualizacion: controles, tienda y enciclopedia
- Runner: botones visuales 40% mayores, mas separados y deteccion por circulo individual. Ya no se usa una zona circular central que excluia partes de los botones.
- Mapa: accesos MENU y TIENDA fijos en la cabecera.
- Tienda: tarjetas legibles en un espacio virtual adaptable, ayudas del laboratorio y cinco apariencias. Se guarda tambien la compra/activacion de apariencia. Su previsualizacion no altera el inventario.
- Laboratorio: botones +5 PULSOS, PURGA y COMODIN con stock visible. Solo consumen ayudas disponibles, guardando el inventario. Las apariencias se aplican a los atomos del laboratorio.
- Enciclopedia: moleculas descubiertas, ocho tips y tres experimentos con pasos, explicacion y cuidados. Navegacion paginada sin necesidad de desbloquear tips/experimentos. Ajuste de texto al panel. Acceso a la fuente ACS mediante navegador externo.
- Correcciones puntuales en fichas antiguas: el agua no disuelve todo, el amoniaco es una base debil; formulas visibles corregidas para acetaldehido y HCl sin cambiar las claves de progreso heredadas.

Experimentos: disolucion de sal, separacion de agua/aceite y recuperacion de sal por evaporacion a temperatura ambiente. Instrucciones originales adaptadas a pequena escala; no son una reproduccion literal de una guia.
Fuentes educativas:
https://www.acs.org/education/resources/k-8/inquiryinaction/fifth-grade.html
https://www.acs.org/education/resources/k-8/inquiryinaction/fifth-grade/chapter-1-investigating-matter-at-the-particle-level/lesson-1-3--dissolving-and-back-again.html
https://www.acs.org/education/celebrating-chemistry-editions/science-safety-tips.html

Verificacion adicional: sintaxis Java de todos los archivos modificados; pruebas de logica de compras, saldo insuficiente, consumo unico de ayudas, apariencia ya adquirida y previsualizacion sin cambiar seleccion. Comprobacion geometrica de zonas tactiles del runner sin solapamiento. La compilacion completa y la prueba visual siguen pendientes en Android Studio/celular.
