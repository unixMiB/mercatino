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
import com.kowalski7cc.botrevolution.types.chat.Chat;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataStore {

    private static Map<Chat, StatesManager<Message>> chats;
    private static List<Long> admins;
    private static Map<String, Advertisement> advertisementMap;
    private static String board;
    public static TelegramBot telegramBot;

    public static void loadDataStore() {
        try {
            var options = new JSONObject(Files.readAllLines(Paths.get("config.json")).stream().collect(Collectors.joining()));
            telegramBot = new TelegramBot(options.getString("token"));
            setBoard(options.getString("board"));
            setAdmins(options.getJSONArray("admins")
                    .toList()
                    .parallelStream()
                    .map(o -> Long.parseLong(o.toString()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            telegramBot = new TelegramBot(System.getenv("MERCATINO_TELEGRAM_TOKEN"));
            setBoard(System.getenv("MERCATINO_BOARD_ID"));
            setAdmins(Optional.ofNullable(System.getenv("MERCATINO_ADMINS_IDS"))
                    .map(s -> List.of(s.split(":")).parallelStream().map(Long::parseLong).collect(Collectors.toList()))
                    .orElse(List.of()));
        }

        advertisementMap = new HashMap<>();
        chats = new HashMap<>();
    }

    public static Map<Chat, StatesManager<Message>> getChats() {
        return chats;
    }

    public static void setChats(Map<Chat, StatesManager<Message>> chats) {
        DataStore.chats = chats;
    }

    public static List<Long> getAdmins() {
        return admins;
    }

    public static void setAdmins(List<Long> admins) {
        DataStore.admins = admins;
    }

    public static Map<String, Advertisement> getAdvertisementMap() {
        return advertisementMap;
    }

    public static void setAdvertisementMap(Map<String, Advertisement> advertisementMap) {
        DataStore.advertisementMap = advertisementMap;
    }

    public static String getBoard() {
        return board;
    }

    public static void setBoard(String board) {
        DataStore.board = board;
    }
}
