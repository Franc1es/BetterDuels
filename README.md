### BetterDuels
Plugin per duelli realistici compatibile dalla versione 1.8 fino all'ultima versione di Minecraft.
Completamente configurabile per adattarsi a qualsiasi esigenza.  
La selezione dei kit avviene tramite una GUI intuitiva che permette la creazione di kit personalizzati.

### Developer
Francies

### Dipendenze
Questo plugin richiede MultiVerse-Core 2 come dipendenza.
Puoi scaricare MultiVerse-Core [qui](https://dev.bukkit.org/projects/multiverse-core).

### Info/Aiuto
Per domande o assistenza, visita il mio [sito web](https://franciesdev.it). :dizzy:

### Comandi
- `/duel <nome_player>`: Avvia un duello con un altro giocatore.
- `/btdreload`: Ricarica il file `config.yml`.
- `/duelaccept <nome_player>`: Accetta una richiesta di duello.
- `/dueldeny <nome_player>`: Rifiuta una richiesta di duello.
- `/duelstats`: Crea la leaderboard per mostrare coloro che ne hanno vinti di più.
- `/duelstatsremove`: Elimina la leaderboard creata.

### Permessi
- `betterduels.start`: Permesso di default per usare il comando `/duel`.
- `betterduels.kit.<nomekit>`: Dà accesso a un kit specifico.
- `betterduels.admin`: Permesso che permette di ricaricare il `config.yml` e eseguire i comandi `/duelstats` e `/duelstatsremove`.
- `betterduels.accept`: Permesso per accettare il duello.
- `betterduels.deny`: Permesso per rifiutare il duello.

### Database
Per creare una leaderboard, il plugin crea automaticamente una tabella
nel tuo database, che devi aver già creato, del quale hai specificato le informazioni nel `config.yml`  
database:  
 host: # metti qui l'ip del tuo database  
 databasename: # nome del database  
 username: # inserisci l'username per accedere al database, ricorda di dare il grant  
 password: # inserisci la password per accedere al database, ricorda di dare il grant  
 flagssl: false # specifica se il database usa il flagSSL
