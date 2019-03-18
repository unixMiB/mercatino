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
        DataStore.initialize();
        DataStore.telegramBot.getReceiver().startPolling();
        DataStore.telegramBot.getMe()
                .ifPresentOrElse(user -> System.out.println("Connected as " + user.getFirstName() + " https://t.me/" + user.getUsername().orElse("")),
                () -> System.exit(1));
        while (DataStore.telegramBot.getReceiver().isPolling()) {
            synchronized (DataStore.telegramBot.getReceiver().getUpdates()) {
                DataStore.telegramBot.getReceiver().getUpdates().wait();
            }

            while (!DataStore.telegramBot.getUpdates().isEmpty())
                CompletableFuture.runAsync(() -> apply(DataStore.telegramBot.getUpdates()
                        .poll(), DataStore.telegramBot))
                        .join();
        }
    }

    private static void printLicense() {
        var version = System.getProperties().getProperty("mercatino.version");
        System.out.println("MercatinoBot - unixMiB https://unixmib.github.io/\n" +
                "Build " + (version==null?"developement":version) + "\n" +
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
                .ifPresent(privateChat -> DataStore.commandManager
                        // TODO Make CommandManager part of BotRevolution Library
                        .runCommandOrElse(getState(privateChat, tg), message,
                                () -> getState(privateChat, tg).apply(message))));


        // Clean up this garbage
        update.getCallbackQuery().ifPresent(callbackQuery -> callbackQuery.getData()
                .ifPresent(s -> {
                    var request = s.split(":");
                    switch (request[0]) {
                        case "delmsg": {
                            // Allow users to delete published post
                            try {
                                tg.deleteMessage()
                                        .setMessageID(Integer.valueOf(request[1]))
                                        .setChatID(DataStore.getBoardID())
                                        .send().get();
                            } catch (TelegramException e) {
                                callbackQuery.getMessage().ifPresent(message -> {
                                    tg.deleteMessage()
                                            .setMessage(message)
                                            .send();
                                    tg.sendMessage()
                                            .setChatID(message.getChat())
                                            .setText("Annuncio non trovato, contatta gli" +
                                                    " amministratori sul gruppo @unixmib")
                                            .setReplyMarkup(new InlineKeyboardBuilder()
                                                    .addRow()
                                                    .buildButton("Gruppo unixMiB")
                                                    .setUrl("https://t.me/unixmib")
                                                    .build().build().build())
                                            .send();
                                });
                            }
                        }
                        break;


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
                            // Send notification to moderator if post not found
                            DataStore.getAdvertisements().computeIfAbsent(request[1], s1 -> {
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

                            // Publish post on channel, get message id, send confirmation with callback id
                            DataStore.getAdvertisements().computeIfPresent(request[1], (s1, advertisement) -> {
                                var msg = tg.sendPhoto()
                                        .setChatID(DataStore.getBoardID())
                                        .setPhoto(advertisement.getPhotoSizes().get(0).getFileID())
                                        .setCaption(advertisement.getTitle() + "\n" + advertisement.getDescription())
                                        .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                .buildButton("Invia richiesta")
                                                // TEST: does int client work as id?
                                                .setCallbackData("contact:" + advertisement.getPublisherOverride()
                                                        .orElse(advertisement.getOwner().getId()))
                                                .build()
                                                .build().addRow().buildButton("Gruppo unixMiB")
                                                .setUrl("https://t.me/unixmib").build().build().build())
                                        .send();

                                // Send confirmation to user
                                msg.ifPresent(message -> tg.sendMessage().setText("Il tuo annuncio \"" + advertisement.getTitle()
                                        + "\" è stato pubblicato")
                                        .setChatID(Long.valueOf(advertisement.getOwner().getId()))
                                        .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                .buildButton("Cancella annuncio")
                                                .setCallbackData("delmsg:" + message.getMessageID())
                                                .build()
                                                .build().build()).send());

                                // Delete moderation message
                                callbackQuery.getMessage().ifPresent(message -> tg.deleteMessage()
                                        .setChatID(message.getChat())
                                        .setMessageID(message)
                                        .send());

                                // Answer to callback
                                try {
                                    tg.answerCallbackQuery()
                                            .setCallbackQueryID(callbackQuery)
                                            .setText("Annuncio pubblicato")
                                            .setCacheTime(1)
                                            .send();
                                } catch (TelegramException e) {
                                    // Query timed out
                                    System.out.println("TIMEOUT QueryManager: " + e.toString() + ", Query ID: " +
                                            callbackQuery.getId());
                                }
                                return null;
                            });
                            break;


                        case "delete":
                            DataStore.getAdvertisements().computeIfAbsent(request[1], s1 -> {
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
                            DataStore.getAdvertisements().computeIfPresent(request[1], (s1, advertisement) -> {
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
                            // Send a message to post publisher, if user doesn't have an username, fail with notification.
                            callbackQuery.getFrom().getUsername().ifPresentOrElse(s1 -> {
                                try {
                                    // Send a message to publisher with user ID in the button
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

                                    // Notify user on successful poke
                                    tg.answerCallbackQuery()
                                            .setCallbackQueryID(callbackQuery)
                                            .setText("Richiesta inviata!")
                                            .setShowAlert(false)
                                            .setCacheTime(10)
                                            .send();
                                } catch (Exception e) {
                                    // If user blocked the bot, notify user
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
                .orElse(BotLogic.loadFSM(new StatesManager<>(), telegramBot)));
    }

}