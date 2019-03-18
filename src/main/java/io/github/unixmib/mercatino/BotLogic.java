/**
 * Copyright (C) 2019 Kowalski7cc
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.unixmib.mercatino;

import com.kowalski7cc.botrevolution.TelegramBot;
import com.kowalski7cc.botrevolution.types.Message;
import com.kowalski7cc.botrevolution.types.repymarkups.inlinekeyboard.InlineKeyboardBuilder;
import com.kowalski7cc.botrevolution.types.repymarkups.replykeyboards.ReplyKeyboardBuilder;
import com.kowalski7cc.botrevolution.types.repymarkups.replykeyboards.ReplyKeyboardRemove;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class BotLogic {

    public static StatesManager<Message> loadFSM(StatesManager<Message> statesManager, TelegramBot telegramBot) {

        statesManager.newState("start", o -> {
            telegramBot.sendMessage().setChatID(o.getChat()).setText("Ciao" + o.getFrom()
                    .map(user -> {
                        return " " + user.getFirstName();
                    })
                    .orElse("") + ", benvenuto al mercatino")
                    .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                            .buildButton("Apri bacheca annunci")
                            .setUrl("https://t.me/unixmib_mercatino")
                            .build()
                            .buildButton("Pubblica annuncio")
                            .setCallbackData("new_advertisement")
                            .build()
                            .build().build())
                    .send();
            return "show_hint";
        });

        statesManager.newState("show_hint", message -> {
            telegramBot.sendMessage().setText("Per iniziare a vendere, premi il tasto \"Pubblica annuncio\"")
                    .setChatID(message.getChat())
                    .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                            .buildButton("Apri bacheca annunci")
                            .setUrl("https://t.me/unixmib_mercatino")
                            .build()
                            .buildButton("Pubblica annuncio")
                            .setCallbackData("new_advertisement")
                            .build()
                            .build().build())
                    .send();
            return null;
        });

        statesManager.newState("new_advertisement", message -> {
            message.getChat().getPrivateChat()
                    .ifPresent(privateChat -> statesManager.store
                            .put("advertisement", new Advertisement(privateChat.toUser())));

            // Check if user is in admin list, then parse parameter and use as Client id in advertisement
            // fix that get() warning with ifPresent()
            if (DataStore.getAdmins().contains((long) message.getFrom().get().getId())) {
                new CommandParser(message).getParameters().ifPresent(s ->
                        getAdvertisementData(statesManager).setClient(Integer.parseInt(s)));
            } else {
                getAdvertisementData(statesManager).setClient(message.getFrom().get().getId());
            }

            telegramBot.sendMessage()
                    .setChatID(message.getChat())
                    .setText("Inviami il titolo della tua inserzione")
                    .send();
            return "read_title";
        });

        statesManager.newState("read_title", message -> message.getText()
                .map(s -> {
                    getAdvertisementData(statesManager).setTitle(s);
                    telegramBot.sendMessage()
                            .setChatID(message.getChat())
                            .setText("Aggiungi una descrizione alla tua inserzione")
                            .send();
                    return "read_description";
                })
                .orElseGet(() -> {
                    telegramBot.sendMessage()
                            .setChatID(message.getChat())
                            .setText("Contenuto non valido, riprova")
                            .send();
                    return null;
                }));

        statesManager.newState("read_description", message -> message.getText()
                .map(s -> {
                    getAdvertisementData(statesManager).setDescription(s);
                    telegramBot.sendMessage()
                            .setChatID(message.getChat())
                            .setText("Allega una foto dell'oggetto che vuoi vendere")
                            .send();
                    return "read_picture";
                })
                .orElseGet(() -> {
                    telegramBot.sendMessage()
                            .setText("Contenuto non valido, riprova")
                            .setChatID(message.getChat())
                            .send();
                    return null;
                }));

        statesManager.newState("read_picture", message -> message.getPhoto()
                .map(photoSizes -> {
                    getAdvertisementData(statesManager).setPhotoSizes(photoSizes);
                    statesManager.store.put("picture", photoSizes);
                    telegramBot.sendMessage()
                            .setText("Ecco una anteprima della tua inserzione")
                            .setChatID(message.getChat())
                            .send();
                    telegramBot.sendPhoto()
                            .setChatID(message.getChat())
                            .setCaption(getAdvertisementData(statesManager).getTitle() + "\n" + getAdvertisementData(statesManager).getDescription())
                            .setPhoto(photoSizes.get(0)
                                    .getFileID())
                            .setReplyMarkup(new ReplyKeyboardBuilder().addRow().buildButton("Pubblica").build()
                                    .buildButton("Annulla").build().build().build())
                            .send();
                    return "read_publish";
                })
                .orElseGet(() -> message.getDocument()
                        .map(document -> {
                            telegramBot.sendMessage()
                                    .setChatID(message.getChat())
                                    .setText("La foto non deve essere inviata come file. Riprova allegandola come immagine.")
                                    .send();
                            return "read_picture";
                        })
                        .orElseGet(() -> {
                            telegramBot.sendMessage()
                                    .setChatID(message.getChat())
                                    .setText("Contenuto non valido, riprova.")
                                    .send();
                            return null;
                        })));


        statesManager.newState("read_publish", message -> message.getText()
                .map(s -> Optional.of(s)
                        .filter(s1 -> s1.equals("Pubblica"))
                        .map(s1 -> {
                            var adv = getAdvertisementData(statesManager);
                            String uuid = UUID.randomUUID().toString();
                            DataStore.getAdvertisements().put(uuid, adv);
                            DataStore.getAdmins().forEach(id -> {
                                try {
                                    telegramBot.sendPhoto()
                                            .setChatID(id.longValue())
                                            .setCaption(adv.getTitle() + "\n" + adv.getDescription())
                                            .setPhoto(adv.getPhotoSizes().get(0).getFileID())
                                            .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                    .buildButton("Approva")
                                                    .setCallbackData("publish:" + uuid)
                                                    .build()
                                                    .buildButton("Cancella")
                                                    .setCallbackData("delete:" + uuid)
                                                    .build()
                                                    .build().build())
                                            .send();
                                } catch (Exception e) {
                                    System.out.println("MODERATION ERROR: Impossibile contattare l'admin " + id);
                                }
                            });

                            telegramBot.sendMessage()
                                    .setChatID(message.getChat())
                                    .setText("Ottimo. Il tuo annuncio sarà presto pubblicato")
                                    .setReplyMarkup(new ReplyKeyboardRemove())
                                    .send();
                            return "show_hint";
                        })
                        .or(() -> Optional.of(s).filter(s1 -> s1.equals("Annulla")).map(s1 -> {
                            telegramBot.sendMessage()
                                    .setChatID(message.getChat())
                                    .setText("Pubblicazione annullata")
                                    .setReplyMarkup(new ReplyKeyboardRemove())
                                    .send();
                            return "show_hint";
                        }))
                        .orElse(null))
                .orElseGet(() -> {
                    telegramBot.sendMessage()
                            .setChatID(message.getChat())
                            .setText("Contenuto non valido, riprova.")
                            .send();
                    return null;
                }));

        statesManager.newState("bacheca", message -> {
            telegramBot.sendMessage().setText("Puoi aprire la bacheca cercando @unixmib_mercatino o premendo il tasto qui sotto")
                    .setChatID(message.getChat())
                    .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                            .buildButton("Apri bacheca annunci")
                            .setUrl("https://t.me/unixmib_mercatino")
                            .build().build().build())
                    .send();
            return "show_hint";
        });

        statesManager.newState("abort", message -> {
            telegramBot.sendMessage().setText("Azione annullata")
                    .setChatID(message.getChat())
                    .setReplyMarkup(new ReplyKeyboardRemove())
                    .send();
            return "show_hint";
        });

        statesManager.newState("selfpromote", message -> {
            Consumer<String> msg = text -> telegramBot.sendMessage()
                    .setChatID(message.getChat())
                    .setText(text)
                    .send();
            Runnable error = () -> msg.accept("Errore durante l'elaborazione");

            DataStore.getAdminsGroup().ifPresentOrElse(s -> message.getFrom()
                    .ifPresentOrElse(user -> telegramBot.getChatMember()
                            .setChatID(s)
                            .setUserID(user)
                            .send()
                            .ifPresentOrElse(chatMember -> {
                                if (chatMember.getStatus().equals("left")) {
                                    msg.accept("Non hai i privilegi per eseguire questa operazione. Questo incidente verrà segnalato.");
                                    System.out.println("SELFPROMOTE FAILED: " + message.getFrom());
                                } else {
                                    DataStore.addAdmin(user.getId());
                                    msg.accept("Privilegi elevati con successo");
                                    System.out.println("SELFPROMOTE SUCCEDED: " + message.getFrom());
                                }
                            }, () -> error.run()), () -> error.run()), () -> error.run());
            return "show_hint";
        });

        statesManager.newState("unknownCommand", message -> {
            telegramBot.sendMessage()
                    .setChatID(message.getChat())
                    .setText("Comando non valido")
                    .send();
            return "show_hint";
        });

        statesManager.setInitialState("start");
        statesManager.reset();

        System.out.println(statesManager);

        return statesManager;
    }

    private static Advertisement getAdvertisementData(StatesManager<Message> statesManager) {
        return ((Advertisement) statesManager.store.get("advertisement"));
    }
}
