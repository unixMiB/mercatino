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

-  `190304`, `190304-alpine`, `alpine`, `latest` (190304/alpine)

-  `190304-windowsservercore`, `190304-windowsservercore-ltsc2019`, `windowsservercore`, `windowsservercore-ltsc2019` (190304/windows/servercore/ltsc2019)

-  `190303`, `190303-alpine` (190303/alpine)

-  `190303-windowsservercore`, `190303-windowsservercore-ltsc2019` (190303/windows/servercore/ltsc2019)

-  `190228`, `190228-alpine` (190228/alpine)

-  `190228-windowsservercore`, `190228-windowsservercore-ltsc2019` (199228/windows/servercore/ltsc2019)

-  `190223`, `190223-stretch`, `stretch` (190223/stretch)

-  `190223-windowsservercore`, `190223-windowsservercore-ltsc2019` (190223/windows/servercore/ltsc2019)

### Shared tags

- `latest`