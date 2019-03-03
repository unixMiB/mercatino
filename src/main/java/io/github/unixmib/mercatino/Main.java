/**
 * Copyright (C) 2019 Kowalski7cc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.unixmib.mercatino;

import com.kowalski7cc.botrevolution.TelegramBot;
import com.kowalski7cc.botrevolution.types.Message;
import com.kowalski7cc.botrevolution.types.Update;
import com.kowalski7cc.botrevolution.types.chat.Chat;
import com.kowalski7cc.botrevolution.types.repymarkups.inlinekeyboard.InlineKeyboardBuilder;
import com.kowalski7cc.botrevolution.utils.decoder.TelegramException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        printLicense();
        DataStore.loadDataStore();
        DataStore.telegramBot.getReceiver().startPolling();
        DataStore.telegramBot.getMe().ifPresentOrElse(user -> System.out.println("Connected as " + user.getFirstName()),
                () -> System.exit(1));
        while (DataStore.telegramBot.getReceiver().isPolling()) {
            synchronized (DataStore.telegramBot.getReceiver().getUpdates()) {
                DataStore.telegramBot.getReceiver().getUpdates().wait();
            }

            while (!DataStore.telegramBot.getUpdates().isEmpty())
                CompletableFuture.runAsync(() -> apply(DataStore.telegramBot.getUpdates().poll(), DataStore.telegramBot)).join();
        }
    }

    private static void printLicense() {
        System.out.println("MercatinoBot - unixMiB https://unixmib.github.io/\n" +
                "Copyright (C) 2019  Kowalski7cc\n" +
                "\n" +
                "This program is free software: you can redistribute it and/or modify\n" +
                "it under the terms of the GNU Affero General Public License as published by\n" +
                "the Free Software Foundation, either version 3 of the License, or\n" +
                "(at your option) any later version.\n" +
                "\n" +
                "This program is distributed in the hope that it will be useful,\n" +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                "GNU Affero General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU Affero General Public License\n" +
                "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n" +
                "---------------------------------------------------------------------------\n");
    }

    private static void apply(Update update, TelegramBot tg) {
        System.out.println(update);

        // handle incoming messages
        update.getMessage().ifPresent(message -> message.getChat()
                .getPrivateChat()
                .ifPresent(privateChat -> new CommandParser(message).ifPresentOrElse((command, parameters) -> {
                    switch (command) {
                        case "start":
                            getState(privateChat, tg).reset().apply(message);
                            break;
                        case "vendi":
                            getState(privateChat, tg).reset().jumpToState("new_advertisement").apply(message);
                            break;
                        case "help":
                            getState(privateChat, tg).reset().jumpToState("show_hint").apply(message);
                            break;
                        case "bacheca":
                            getState(privateChat, tg).reset().jumpToState("bacheca").apply(message);
                            break;
                        case "annulla":
                            getState(privateChat, tg).reset().jumpToState("abort").apply(message);
                            break;
                        default:
                            tg.sendMessage().setChatID(message.getChat()).setText("Comando non valido").send();
                            break;
                    }
                }, () -> getState(privateChat, tg).apply(message))));

        update.getCallbackQuery().ifPresent(callbackQuery -> callbackQuery.getData()
                .ifPresent(s -> {
                    var request = s.split(":");
                    switch (request[0]) {
                        case "new_advertisement":
                            tg.answerCallbackQuery()
                                    .setCallbackQueryID(callbackQuery)
                                    .setCacheTime(10)
                                    .send();
                            callbackQuery.getMessage().ifPresent(message -> getState(message.getChat(), tg).
                                    jumpToState("new_advertisement")
                                    .apply(message));
                            break;
                        case "publish":
                            DataStore.getAdvertisementMap().computeIfAbsent(request[1], s1 -> {
                                tg.answerCallbackQuery()
                                        .setCallbackQueryID(callbackQuery)
                                        .setText("Annuncio non trovato, probabilmente è già stata effettuata la pubblicazione")
                                        .setCacheTime(1)
                                        .send();
                                callbackQuery.getMessage()
                                        .ifPresent(message -> tg.deleteMessage()
                                                .setMessageID(message)
                                                .setChatID(message.getChat())
                                                .send());
                                return null;
                            });
                            DataStore.getAdvertisementMap().computeIfPresent(request[1], (s1, advertisement) -> {
                                tg.sendPhoto()
                                        .setChatID(DataStore.getBoard())
                                        .setPhoto(advertisement.getPhotoSizes().get(0).getFileID())
                                        .setCaption(advertisement.getTitle() + "\n" + advertisement.getDescription())
                                        .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                .buildButton("Invia richiesta")
                                                .setCallbackData("contact:" + advertisement.getOwner().getId())
                                                .build()
                                                .build().addRow().buildButton("Gruppo unixMiB")
                                                .setUrl("https://t.me/unixmib").build().build().build())
                                        .send();
                                callbackQuery.getMessage().ifPresent(message -> tg.deleteMessage()
                                        .setChatID(message.getChat())
                                        .setMessageID(message)
                                        .send());
                                try {
                                    tg.answerCallbackQuery()
                                            .setCallbackQueryID(callbackQuery)
                                            .setText("Annuncio pubblicato")
                                            .setCacheTime(1)
                                            .send();
                                } catch (TelegramException e) {
                                    // Query timed out
                                    System.out.println("QueryManager: " + e.toString() + ", Query ID: " +
                                            callbackQuery.getId());
                                }
                                return null;
                            });
                            break;
                        case "delete":
                            DataStore.getAdvertisementMap().computeIfAbsent(request[1], s1 -> {
                                try {
                                    tg.answerCallbackQuery()
                                            .setCallbackQueryID(callbackQuery)
                                            .setText("Annuncio non trovato, probabilmente è già stata effettuata la pubblicazione")
                                            .setCacheTime(1)
                                            .send();
                                    callbackQuery.getMessage()
                                            .ifPresent(message -> tg.deleteMessage()
                                                    .setMessage(message)
                                                    .send());
                                    return null;
                                } catch (Exception e) {
                                    return null;
                                }
                            });
                            DataStore.getAdvertisementMap().computeIfPresent(request[1], (s1, advertisement) -> {
                                tg.answerCallbackQuery()
                                        .setCallbackQueryID(callbackQuery)
                                        .setText("Annuncio cancellato")
                                        .setCacheTime(1)
                                        .send();
                                callbackQuery.getMessage().ifPresent(message -> tg.deleteMessage()
                                        .setChatID(message.getChat())
                                        .setMessageID(message)
                                        .send());
                                return null;
                            });
                            break;
                        case "contact":
                            callbackQuery.getFrom().getUsername().ifPresentOrElse(s1 -> {
                                try {
                                    tg.sendMessage()
                                            .setChatID(request[1])
                                            .setText("Ciao, hai avuto una richiesta da " + callbackQuery.getFrom()
                                                    .getFirstName() + "!")
                                            .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                    .buildButton("Contattalo subito")
                                                    .setUrl("https://t.me/" + s1)
                                                    .build()
                                                    .build().build())
                                            .send();
                                    tg.answerCallbackQuery()
                                            .setCallbackQueryID(callbackQuery)
                                            .setText("Richiesta inviata!")
                                            .setShowAlert(false)
                                            .setCacheTime(10)
                                            .send();
                                } catch (Exception e) {
                                    try {
                                        tg.answerCallbackQuery()
                                                .setCallbackQueryID(callbackQuery)
                                                .setText("Errore nella richiesta, probabilmente l'utente non" +
                                                        " è più su telegram o ha bloccato il bot")
                                                .setShowAlert(false)
                                                .setCacheTime(10)
                                                .send();
                                    } catch (Exception e1) {
                                        // oof
                                    }
                                }
                            }, () -> tg.answerCallbackQuery()
                                    .setCallbackQueryID(callbackQuery)
                                    .setText("Accipigna, per poter inviare richieste devi avere un username!")
                                    .setShowAlert(true)
                                    .setCacheTime(10)
                                    .send());

                            break;
                        default:
                            tg.answerCallbackQuery()
                                    .setCallbackQueryID(callbackQuery)
                                    .setText("Si è verificato un errore nella richiesta")
                                    .setCacheTime(200)
                                    .setShowAlert(false)
                                    .send();
                            break;
                    }
                }));
    }

    private static StatesManager<Message> getState(Chat chat, TelegramBot telegramBot) {
        return DataStore.getChats().compute(chat, (chat1, statesManager) -> Optional.ofNullable(statesManager)
                .orElse(BotLogic.load(new StatesManager<>(), telegramBot)));
    }

}