# Progetto di Tecnologie per il Web 2024
Progetto del corso di Tecnologie Informatiche per il Web del Politecnico di Milano (A.A.2023-2024).

Valutazione ottenuta: 30/30. Il progetto è consultabile tramite i file sorgente nelle due versioni (Pure HTML e RIA) presenti in questa repository. Sono anche disponibili la specifica del progetto (fornita dal professore) e la documentazione da realizzare per la valutazione.

## Testo del problema - Esercizio 2: Gestione documenti
### Versione pure HTML
L’applicazione supporta registrazione e login mediante una pagina pubblica con opportune form. La registrazione richiede l’inserimento di username, indirizzo di email e password e controlla la validità sintattica dell’indirizzo di email e l’uguaglianza tra i campi “password” e “ripeti password”. La registrazione controlla l’unicità dello username.

Una cartella ha un proprietario, un nome e una data di creazione e può contenere altre cartelle e/o documenti. Un documento ha un proprietario, nome, una data di creazione, un sommario e un tipo. Quando l’utente accede all’applicazione appare una HOME PAGE che contiene un albero delle proprie cartelle e delle sottocartelle.

<p align="center">
  <img src="https://github.com/AriCalam/Progetto-TIW-2024-Public/blob/main/EsempioAlberoCartelle.png?raw=true" width=65% height=65%>
</p>

Nell’HOME PAGE l’utente può selezionare una cartella e accedere a una pagina CONTENUTI che mostra l’elenco delle cartelle e dei documenti di una cartella. Ogni documento in elenco ha due link: accedi e sposta. Quando l’utente seleziona il link accedi, appare una pagina DOCUMENTO (nella stessa finestra e tab del browser) che mostra tutti i dati del documento selezionato. Quando l’utente seleziona il link sposta, appare la HOME PAGE con l’albero delle cartelle; in questo caso la pagina mostra il messaggio “Stai spostando il documento X dalla cartella Y. Scegli la cartella di destinazione”, la cartella a cui appartiene il documento da spostare NON è selezionabile e il suo nome è evidenziato (per esempio con un colore diverso). Quando l’utente seleziona la cartella di destinazione, il documento è spostato dalla cartella di origine a quella di destinazione e appare la pagina CONTENUTI che mostra il contenuto aggiornato della cartella di destinazione. Ogni pagina, tranne la HOME PAGE, contiene un collegamento per tornare
alla pagina precedente. L’applicazione consente il logout dell’utente da qualsiasi pagina. Una pagina GESTIONE CONTENUTI raggiungibile dalla HOME PAGE permette all’utente di creare una cartella di primo livello, una cartella all’interno di una cartella esistente e un documento all’interno di una cartella. L’applicazione non richiede la gestione dell’upload dei documenti.

### Versione con JavaScript
Si realizzi un’applicazione client-server web che modifica le specifiche precedenti come segue:
- L’applicazione supporta registrazione e login mediante una pagina pubblica con opportune form. La registrazione controlla la validità sintattica dell’indirizzo di email e l’uguaglianza tra i campi “password” e “ripeti password”, anche a lato client. La registrazione controlla l’unicità dello username.
- Dopo il login dell’utente, l’intera applicazione è realizzata con un’unica pagina.
- Ogni interazione dell’utente è gestita senza ricaricare completamente la pagina, ma produce l’invocazione asincrona del server e l’eventuale modifica del contenuto da aggiornare a seguito dell’evento.
- Errori a lato server devono essere segnalati mediante un messaggio di allerta all’interno della pagina.
- La funzione di spostamento di un documento è realizzata mediante drag and drop.
- La funzione di creazione di una sottocartella è realizzata nella pagina HOME mediante un bottone AGGIUNGI SOTTOCARTELLA posto di fianco ad ogni cartella. La pressione del bottone fa apparire un campo di input per l’inserimento del nome della cartella da inserire.
- La funzione di creazione di un documento è realizzata nella pagina HOME mediante un bottone AGGIUNGI DOCUMENTO posto di fianco ad ogni cartella. La pressione del bottone fa apparire una form di input per l’inserimento dei dati del documento.
- Si aggiunge una cartella denominata “cestino”. Il drag and drop di un documento o di una cartella nel cestino comporta la cancellazione. Prima di inviare il comando di cancellazione al server l’utente vede una finestra modale di conferma e può decidere se annullare l’operazione o procedere. La cancellazione di una cartella comporta la cancellazione integrale e ricorsiva del contenuto dalla base di dati (documenti e cartelle).

## Istruzioni
La presentazione viene effettuata online tramite MS Teams, è individuale e richiede:
- L’invio almeno tre giorni prima della data di presentazione della documentazione del progetto;
- La redazione della documentazione secondo gli esempi di esercizi mostrati durante le lezioni e le esercitazioni e già disponibili nel sito WeBeep del corso;
- L’effettuazione di una dimostrazione del progetto, sia nella versione pure HTML sia nella versione con JavaScript;
- La discussione del codice con eventuali domande sulle motivazioni della progettazione e della codifica.

La dimostrazione richiede la preparazione di una base di dati con contenuto sufficiente alla dimostrazione di *tutti* i possibili scenari di uso dell’applicazione.
Entrambe le versioni devono comprendere anche le regole CSS per la presentazione. Tuttavia l’estetica dell’interfaccia utente non influisce sulla valutazione del progetto, che si basa esclusivamente sulla funzionalità.
Inoltre si richiede di sapere utilizzare le funzioni di zoom dell’ambiente di sviluppo e del debugger del browser per consentire la visualizzazione ottimale del codice da remoto e di impostare il tema chiaro per entrambi (sfondo bianco).

Si consiglia di effettuare una prova su MS Teams con un collega per verificare che il contenuto dello schermo risulti sufficientemente leggibile.

## Commenti generali
- Le versioni pure HTML e JavaScript sono da realizzarsi come applicazioni Web distinte.
- Eventuali funzioni non richieste di gestione dei dati (es, modifica o cancellazione) possono essere realizzate se comode per il testing ma sono opzionali e non valutate.
- Il controllo di validità dei parametri deve essere fatto sia lato client sia a lato server. Non si deve consentire a un utente di fare operazioni che il suo ruolo non permette. Non si deve consentire a un utente malintenzionato di violare la sicurezza dell’applicazione mediante l’invio di valori scorretti dei parametri.
