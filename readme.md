# Mercatino unixMiB

## Che cosa è il mercatino

Il mercatino dell'usato di unixMiB ti permette di creare inserzioni per vendere quello che non usi più.

## Cosa ho bisogno per accedere al mercatino

Per utilizzare il mercatino è sufficiente un account [Telegram](https://telegram.org/) per iniziare subito a chattare.

## Come accedo al mercatino

Per iniziare una chat con il bot recati alla pagina [https://t.me/unixmib_mercatino_bot](https://t.me/unixmib_mercatino_bot) o cerca [@unixmib_mercatino_bot](https://t.me/unixmib_mercatino_bot) in Telegram

## Configurazione del bot

- Bot token di Telegram da [@botfather](https://t.me/botfather)
- ID del canale da usare come bacheca del mercatino
- ID degli amministratori iniziali (opzionale)
- ID del gruppo di amministrazione (opzionale)


### Parametri mediante file JSON

`config.json`

```json
{
  "token": "Token del bot",
  "board": "ID del canale usato come bacheca",
  "admins": [123456, 123456, 123456],
  "group": "ID del gruppo di amministratori"
}
```

### Parametri mediante variabili d'ambiente

Configurazione consigliata con un container Docker

- `MERCATUNO_TOKEN` = "Token del bot"
- `MERCATINO_BOARD` = "ID del canale usato come bacheca"
- `MERCATINO_ADMINS` = 123456:123456:123456
- `MERCATINO_GROUP` = "ID del gruppo di amministratori"

Un esempio nella riga di comando sarà:

```bash
java -DMERCATUNO_TOKEN=token -DMERCATINO_BOARD=id -DMERCATINO_ADMINS=id:id:id -DMERCATINO_GROUP=id -jar mercatino.jar
```

### Parametri mediante argomento

- `-t`: Token del bot 
- `-b`: ID del canale usato come bacheca
- `-a`: Stringa di amministratori separata da ':'
- `-g`: ID del gruppo di amministratori

Un esempio nella riga di comando sarà:

```bash
java -jar mercatino.jar -t token -b id -a 123456:123456:123456 -g id
```

## Docker container tags

-  `190318`, `190318-slim`, `slim` (190318/slim)

-  `190318-windowsservercore-1809`, `windowsservercore-1809` (190318/windows/servercore/1809)

### Shared tags

- `latest`
- `190318`
  - Debian Strech Slim
  - Windows Server Core 1809
