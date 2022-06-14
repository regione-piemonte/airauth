# Prodotto
Dbauth2

# Descrizione del prodotto
Questo prodotto fa parte della suite **SRRQA - Rilevamento della Qualità dell'Aria**.

Database preposto alla gestione degli utenti del prodotto webauth. 

# Configurazioni iniziali 
E' necessario che sia installato Postgres. La versione su cui è stata testata la banca dati è Postgres 12.

# Getting Started 
Eseguire il target **distribution** di ANT (tramite OpenJDK 1.8).

Per impostare eventuali parametri tipici di ambienti di test o produzione e' possibile specificare la proprieta':
* `-Dtarget=prod-rp-01`: per deployare su ambiente di produzione (file di properties `prod.properties`)

L'esecuzione di questo target crea un file tar nella cartella `dist/prod` del workspace.


# Prerequisiti di sistema 
Fare riferimento al file BOM.csv per verificare l'elenco delle librerie esterne utilizzate in questo software.

# Installazione 

* Eseguire come utente Postgres la creazione dell'utente dbauth come indicato nel file  `src\sql\createdbauth_user.sql`.

* Eseguire la creazione della banca dati come indicato nel file `src\sql\dbauth2_create.sql`.

* Inizializzare la banca dati come indicato nel file `src\sql\init_tab.sql`.

L'utente gia' presente in db è test con password 1234512

# Versioning
Per il versionamento del software si usa la tecnica Semantic Versioning (http://semver.org).

# Authors
La lista delle persone che hanno partecipato alla realizzazione del software sono  elencate nel file AUTHORS.txt.

# Copyrights
L'elenco dei titolari del software sono indicati nel file Copyrights.txt

# License 
SPDX-License-Identifier: EUPL-1.2-or-later

Vedere il file LICENSE per i dettagli.