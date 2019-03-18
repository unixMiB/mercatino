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

public enum Command implements BotCommand {

    START("start", "start"),
    VENDI("vendi", "new_advertisement"),
    HELP("help", "show_hint"),
    BACHECA("bacheca", "bacheca"),
    ANNULLA("annulla", "abort"),
    SELFPROMOTE("selfpromote", "selfpromote");

    private String command;
    private String state;

    Command(String command, String state) {
        this.command = command;
        this.state = state;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getState() {
        return state;
    }
}
