# Prodotto
WebAuth

# Descrizione del prodotto
Questo prodotto fa parte della suite **SRRQA - Rilevamento della Qualità dell'Aria**.

Software sviluppato in Java che realizza l'interfaccia grafica per la gestione della profilazione utenti. 

# Configurazioni iniziali 
E' necessario che sia stata predisposta la banca dati su cui si appoggia l'applicativo descritta nella componente DBAUTH2.
L'applicativo è stato testato su un server Apache 2.4. con Application Server Apache Tomcat 8.5.

# Getting Started 
Eseguire il target **dist-webauth2** di ANT (tramite OpenJDK 1.8).

Per impostare eventuali parametri tipici di ambienti di test o produzione e' possibile specificare la proprieta':
* `-Dtarget=prod-rp-01`: per deployare su ambiente di produzione (file di properties `prod-rp-01.properties`)

L'esecuzione di questo target crea un file war nella cartella `dist/webauth` del workspace.


# Prerequisiti di sistema 
Fare riferimento al file BOM.csv per verificare l'elenco delle librerie esterne utilizzate in questo software.

# Installazione 

Copiare il file webauth2.war creato con il target di ANT nella cartella `webapp` di Tomcat.

Configurare Apache con le seguenti righe:

```bash
ProxyPass /ariaweb/webauth2/servlet ajp://localhost:8009/webauth2/
ProxyPassReverse /ariaweb/webauth2/servlet ajp://localhost:8009/webauth2/
```
E riavviare Apache.


## Avvio interfaccia grafica
L'interfaccia grafica dell'autenticazione è accessibile all'indirizzo:

`https://IP/webauth2/servlet/ariaweb_portal?main`

Per accedere è necessario inserire utente e password inseriti sul database Dbauth2. Il primo utente e la sua password devono essere inseriti a mano sul db.

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