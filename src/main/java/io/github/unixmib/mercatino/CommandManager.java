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

import com.kowalski7cc.botrevolution.types.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO This class should probably moved to BotRevolution Library
public class CommandManager {

    private Map<String, String> commands;
    private String errorState;

    public CommandManager() {
        commands = new HashMap<>();
    }

    public <T extends BotCommand> CommandManager loadCommandsFromKeys(T[] values) {
        List.of(values).forEach(t -> commands.put(t.getCommand(), t.getState()));
        return this;
    }

    public CommandManager newCommand(String command, String state) {
        commands.put(command.startsWith("/")?command.substring(1):command, state);
        return this;
    }

    public CommandManager setErrorState(String errorState) {
        this.errorState = errorState;
        return this;
    }

    public void runCommand(StatesManager statesManager, Message message) {
        new CommandParser(message).ifPresent((command, parameters) -> statesManager.reset()
                .jumpToState(commands.getOrDefault(command, errorState))
                .apply(message));
    }

    public void runCommandOrElse(StatesManager statesManager, Message message, Runnable runnable) {
        new CommandParser(message).ifPresentOrElse((command, parameters) -> statesManager.reset()
                .jumpToState(command)
                .apply(message), runnable);
    }


}
