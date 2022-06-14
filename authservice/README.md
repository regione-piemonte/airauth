# Prodotto
Authservice

# Descrizione del prodotto
Questo prodotto fa parte della suite **SRRQA - Rilevamento della Qualità dell'Aria**.

Servizio REST per l'autenticazione

# Configurazioni iniziali 
E' necessario che sia stata predisposta la banca dati su cui si appoggia l'applicativo descritta nella componente DBAUTH2.
L'applicativo è stato testato su un server Apache 2.4. con Application Server Apache Tomcat 8.5.

# Getting Started 
Eseguire il target **dist-service** di ANT (tramite OpenJDK 1.8).

L'esecuzione di questo target crea un file war nella cartella `dist/bin` del workspace.


# Prerequisiti di sistema 
Fare riferimento al file BOM.csv per verificare l'elenco delle librerie esterne utilizzate in questo software.

# Installazione 

Copiare il file authdbservice.war creato con il target di ANT nella cartella `webapp` di Tomcat.

Configurare Apache con le seguenti righe:

```bash
ProxyPass /ariaweb/awws/authdbservice ajp://localhost:8009/authdbservice/
ProxyPassReverse /ariaweb/awws/authdbservice ajp://localhost:8009/authdbservice/
```
E riavviare Apache.


## Utilizzo dei servizi
Per l'utilizzo dei servizi si rimanda al file authservice.yaml presente nella cartella doc del progetto.

# Esecuzione dei test
Sono stati eseguiti test di vulnerabilità DAST e SAST e non sono state rilevate vulnerabilita' gravi.

# Versioning
Per il versionamento del software si usa la tecnica Semantic Versioning (http://semver.org).

# Authors
La lista delle persone che hanno partecipato alla realizzazione del software sono  elencate nel file AUTHORS.txt.

# Copyrights
L'elenco dei titolari del software sono indicati nel file Copyrights.txt

# License 
SPDX-License-Identifier: EUPL-1.2-or-later

Vedere il file LICENSE per i dettagli.