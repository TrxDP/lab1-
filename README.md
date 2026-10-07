# Yu-Gi-Oh! Duel Lite

Duelo simplificado entre el jugador y la máquina con cartas Monster aleatorias de la API YGOProDeck.
Desarrollo de Software III
Estudiantes:
Kevin Castillo Perez
Diego Andres Bolaños Isiquita

## Instrucciones de ejecución

Requisitos: Java 11 o superior, Maven 3.6+ y conexión a internet.

Desde la carpeta del proyecto:

bash
mvn compile exec:java



También puede abrirse en IntelliJ IDEA como proyecto Maven (File > Open > pom.xml) y ejecutar com.duellite.Main.

Cómo se juega: pulsar *Iniciar duelo, esperar a que se carguen las 6 cartas, y en cada ronda pulsar **Atacar* o *Defender* en la carta elegida.
El primero en llegar a 2 victorias gana.

Reglas:

- Cada carta se juega en posición de ataque (usa su ATK) o de defensa (usa su DEF); la máquina elige carta y posición al azar.
- Se comparan los valores usados: ATK vs ATK, ATK vs DEF, DEF vs ATK y DEF vs DEF. Gana el valor mayor; si son iguales, la ronda es empate y nadie suma punto.
- Una carta usada no puede volver a jugarse.
- El primer turno es aleatorio y luego se alterna. Si se agotan las 3 cartas sin que nadie llegue a 2 victorias, gana quien tenga más puntos (o hay empate).
- Se descartan las cartas que no son Monster y los monstruos sin DEF (Link), pidiendo otra carta a la API.


## Capturas de pantalla

<p align="center">

  <img src="/capturas/captura1.png" alt="Captura 1" width="600">

  <img src="/capturas/captura2.png" alt="Captura 2" width="600">

  <img src="/capturas/captura3.png" alt="Captura 3" width="600">
</p>

---