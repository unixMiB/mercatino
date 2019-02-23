/**
 * Copyright (C) 2019 kowalski7cc
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
import com.kowalski7cc.botrevolution.types.MessageEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CommandParser {

    private Message message;

    public CommandParser(Message message) {
        this.message = Objects.requireNonNull(message);
    }

    public Optional<String> getCommand() {
        return message.getEntities()
                .orElse(Collections.emptyList())
                .stream()
                .filter(messageEntity -> messageEntity.getType()
                        .equals(MessageEntity.MessageEntityType.BOT_COMMAND))
                .map(messageEntity -> message.getText()
                        .filter(s -> s.length()>messageEntity.getOffset()+messageEntity.getOffset())
                        .map(s -> s.substring(messageEntity.getOffset()+1, messageEntity.getOffset()+messageEntity.getLength())))
                .findFirst()
                .orElse(Optional.empty());
    }

    public Optional<String> getParameters() {
        return getCommand()
                .map(s -> message.getText().map(s1 -> Arrays.asList(s1.split(s + " "))))
                .map(strings -> strings.orElse(Collections.emptyList()))
                .filter(strings -> strings.size()>1).map(strings -> strings.get(1));
    }

    public void ifPresent(BiConsumer<String, Optional<String>> biFunction) {
        getCommand().ifPresent(s -> biFunction.accept(s, getParameters()));
    }

    public void ifPresentOrElse(BiConsumer<String, Optional<String>> biFunction, Runnable runnable) {
        getCommand().ifPresentOrElse(s -> biFunction.accept(s, getParameters()), runnable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandParser that = (CommandParser) o;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "CommandParser{" +
                "message=" + message +
                '}';
    }
}
