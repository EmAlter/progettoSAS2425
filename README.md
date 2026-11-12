# Informazioni generali
Il progetto d'esame ha previsto la realizzazione solo di una parte dell'intero sistema, cioè il caso d'uso **Gestire il personale**.
Si è partiti dalla realizzazione e scrittura di documentazione seguendo la metodologia **Unified Process** (UP), fino ad arrivare alla realizzazione del codice sorgente in Java.

L'obiettivo non era quello di realizzare un sistema completo, ma di dimostrare la capacità di progettare e sviluppare un software seguendo le fasi del ciclo di vita del software.

Nella cartella `files` sono presenti due documenti importanti:

* **Testo Progetto** che rappresenta l'obiettivo e le caratteristiche del progetto da realizzare.
* **Testo finale** che contiene il merge di tutte le fasi di progettazione e sviluppo di software del caso d'uso **Gestire il personale**.

Nella cartella `src` è presente il codice del progetto:

* `src/main` contiene il codice sorgente.
* `src/test` contiene i test.

# Istruzioni: Esecuzione del progetto CatERing
## Compilare il progetto
#### Metodo A – Terminale
```bash
mvn clean compile
```
#### Metodo B – Interfaccia grafica Maven
1. Apri la vista laterale o in basso denominata Maven (la posizione puo' cambiare a seconda della versione di VS Code)
2. Espandi catering > Lifecycle
3. Fai clic su compile

## Eseguire il progetto
#### Metodo A – Da terminale
```bash
mvn exec:java
```
#### Metodo B – Da Visual Studio Code
1. Apri la classe CatERing.java
2. Clicca sull’icona ▶️ accanto al metodo main oppure sul bottone Run sopra al metodo main

## Eseguire i test
#### Metodo A – Terminale
```bash
mvn test
```
#### Metodo B – Interfaccia grafica Maven
1. Apri la vista laterale o in basso denominata Maven (la posizione puo' cambiare a seconda della versione di VS Code)
2. Espandi catering > Lifecycle
3. Fai clic su test