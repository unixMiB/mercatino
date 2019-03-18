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

/**
 * This class is used from DataStore class
 */
public enum ParameterKey {

    TOKEN("token", "MERCATINO_TOKEN", "t"),
    BOARD("board", "MERCATINO_BOARD", "b"),
    ADMINS("admins", "MERCATINO_ADMINS", "a"),
    ADMINS_GROUP("group", "MERCATINO_GROUP", "g");

    private String json;
    private String env;
    private String param;

    ParameterKey(String json, String env, String param) {
        this.json = json;
        this.env = env;
        this.param = param;
    }

    public String getJson() {
        return json;
    }

    public String getEnv() {
        return env;
    }

    public String getParam() {
        return param;
    }
}
