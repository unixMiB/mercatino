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

import java.util.*;
import java.util.function.Function;

// TODO This class should probably moved to BotRevolution Library
public class StatesManager <Supply> {

    private Map<String, Function<Supply, String>> states;
    private Function<Supply, String> currentState;
    private Function<Supply, String> initialState;
    public Map<String, Object> store;

    public StatesManager() {
        states = new HashMap<>();
        store = new HashMap<>();
        initialState = null;
        currentState = null;
    }

    public StatesManager<Supply> newState(String name, Function<Supply, String> function) {
        states.put(name, function);
        return this;
    }

    public StatesManager<Supply> setInitialState(String name) {
        initialState = states.getOrDefault(name, initialState);
        currentState = Optional.ofNullable(currentState).orElse(initialState);
        return this;
    }

    public StatesManager<Supply> jumpToState(String state) {

        currentState = states.getOrDefault(state, currentState);
        return this;
    }

    public StatesManager<Supply> reset() {
        store.clear();
        currentState = initialState;
        return this;
    }

    public StatesManager<Supply> apply(Supply supply) {
        currentState = Optional.ofNullable(currentState)
                .or(() -> Optional.of(initialState))
                .map(supplyStringFunction -> states.getOrDefault(supplyStringFunction.apply(supply), supplyStringFunction))
                .get();
        return this;
    }

    public StatesManager<Supply> removeState(String state) {
        states.remove(state);
        return this;
    }

    @Override
    public String toString() {
        return "StatesManager{" +
                "states=" + states +
                ", currentState=" + currentState +
                ", initialState=" + initialState +
                ", store=" + store +
                '}';
    }
}