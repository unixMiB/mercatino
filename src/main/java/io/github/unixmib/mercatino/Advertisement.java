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

import com.kowalski7cc.botrevolution.types.User;
import com.kowalski7cc.botrevolution.types.media.PhotoSize;

import java.util.List;
import java.util.Optional;

public class Advertisement {

    private User owner;
    private Optional<Integer> publisherOverride;
    private List<PhotoSize> photoSizes;
    private String title;
    private String description;

    public Advertisement(User owner) {
        this.owner = owner;
    }

    public Advertisement(User owner, List<PhotoSize> photoSizes, String title, String description) {
        this.owner = owner;
        this.photoSizes = photoSizes;
        this.title = title;
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public Advertisement setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public Optional<Integer> getPublisherOverride() { return publisherOverride;}

    public Advertisement setPublisherOverride(Optional<Integer> publisherOverride) {
        this.publisherOverride = publisherOverride;
        return this;
    }

    public List<PhotoSize> getPhotoSizes() {
        return photoSizes;
    }

    public Advertisement setPhotoSizes(List<PhotoSize> photoSizes) {
        this.photoSizes = photoSizes;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Advertisement setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Advertisement setDescription(String description) {
        this.description = description;
        return this;
    }
}
