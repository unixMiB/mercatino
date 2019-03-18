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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {

    private static Map<Chat, StatesManager<Message>> chats;
    private static List<Integer> admins;
    private static Map<String, Advertisement> advertisementMap;
    private static Optional<String> adminsGroup;
    private static String board;
    public static TelegramBot telegramBot;
    public final static String CONFIG_FILE_NAME = "config.json";
    public static CommandManager commandManager;


    /**
     * Main method called fot initializing data store
     */
    public static void initialize() {
        try {
            loadConfigFromFile(new File(CONFIG_FILE_NAME));
        } catch (IOException e) {
            loadConfigFromEnvironment();
        }
        advertisementMap = new HashMap<>();
        chats = new HashMap<>();
        commandManager = new CommandManager().loadCommandsFromKeys(Command.values());
    }

    /**
     * Method used to loadFSM bot configuration from provided JSON file
     * @param configFile File as configuration input
     * @throws IOException when configFile is not found or unable to read
     * @throws JSONException when configFile is malformed
     */
    private static void loadConfigFromFile(File configFile) throws IOException, JSONException {
        var data = new JSONObject(Files.readAllLines(configFile.toPath()).stream()
                .collect(Collectors.joining()));
        telegramBot = new TelegramBot(data.getString(ParameterKey.TOKEN.getJson()));
        board = data.getString(ParameterKey.BOARD.getJson());
        adminsGroup = Optional.ofNullable(data.optString(ParameterKey.ADMINS_GROUP.getJson()));
        // Admins can be not provided, they can self promote if they belong to admin group
        admins = Optional.ofNullable(data.optJSONArray(ParameterKey.ADMINS.getJson()))
                .stream()
                .map(JSONArray::toList)
                .map(o -> Integer.parseInt(o.toString()))
                .collect(Collectors.toList());
    }

    /**
     *
     * @throws NullPointerException when a required env variable is not found
     */
    private static void loadConfigFromEnvironment() throws NullPointerException {
        telegramBot = new TelegramBot(Objects.requireNonNull(System.getenv(ParameterKey.TOKEN.getEnv()),
                "Token not provided"));
        setBoardID(Objects.requireNonNull(System.getenv(ParameterKey.BOARD.getEnv()),
                "Board ID not provided"));
        // Admins can be not provided, they can self promote if they belong to admin group
        admins = Optional.ofNullable(System.getenv(ParameterKey.ADMINS.getEnv()))
                .map(s -> List.of(s.split(":"))
                        .stream()
                        .map(Integer::parseInt)
                        .collect(Collectors.toList()))
                .orElse(List.of());
        adminsGroup = Optional.ofNullable(System.getenv(ParameterKey.ADMINS_GROUP.getEnv()));
    }

    private static void loadConfigFromArument(Optional<String> argument) throws NullPointerException {
        argument.map(s -> s.split(" ")).orElseThrow(() -> new NullPointerException());
    }

    public static Map<Chat, StatesManager<Message>> getChats() {
        return chats;
    }

    public static List<Integer> getAdmins() {
        return admins;
    }

    public static void addAdmin(Integer id) {
        admins.add(id);
    }

    public static void removeAdmin(Long id) {
        admins.remove(id);
    }

    public static Optional<String> getAdminsGroup() {
        return adminsGroup;
    }

    public static Map<String, Advertisement> getAdvertisements() {
        return advertisementMap;
    }

    public static String getBoardID() {
        return board;
    }

    public static void setBoardID(String board) {
        DataStore.board = board;
    }
}
