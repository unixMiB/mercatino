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
import com.kowalski7cc.botrevolution.types.repymarkups.inlinekeyboard.InlineKeyboardBuilder;
import com.kowalski7cc.botrevolution.types.repymarkups.replykeyboards.ReplyKeyboardBuilder;
import com.kowalski7cc.botrevolution.types.repymarkups.replykeyboards.ReplyKeyboardRemove;

import java.util.Optional;
import java.util.UUID;

public class BotLogic {

    public static StatesManager<Message> load(StatesManager<Message> statesManager, TelegramBot telegramBot) {

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
                            DataStore.getAdvertisementMap().put(uuid, adv);
                            DataStore.getAdmins().forEach(aLong -> {
                                try {
                                    telegramBot.sendPhoto()
                                            .setChatID(aLong)
                                            .setCaption(adv.getTitle() + "\n" + adv.getDescription())
                                            .setPhoto(adv.getPhotoSizes().get(0).getFileID())
                                            .setReplyMarkup(new InlineKeyboardBuilder().addRow()
                                                    .buildButton("Approva")
                                                    .setCallbackData("publish:" + uuid)
                                                    .build()
                                                    .buildButton("Cancella")
                                                    .setCallbackData("delete:"+uuid)
                                                    .build()
                                                    .build().build())
                                            .send();
                                } catch (Exception e) {
                                    System.out.println("Inpossibile contattare l'admin " + aLong);
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

        statesManager.setInitialState("start");
        statesManager.reset();

        System.out.println(statesManager);

        return statesManager;
    }

    private static Advertisement getAdvertisementData(StatesManager<Message> statesManager) {
        return ((Advertisement) statesManager.store.get("advertisement"));
    }
}
