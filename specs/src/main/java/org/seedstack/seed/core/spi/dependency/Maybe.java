/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.dependency;

/**
 * A generic class that represented a value that could be set or not.
 * 
 * @author thierry.bouvet@mpsa.com
 * 
 * @param <T> the optional value 
 */
public class Maybe<T> {

    /**
     * The value
     */
    private final T value;

    /**
     * Value to check or null
     * @param optionalValue the optional value or null
     */
    public Maybe(T optionalValue) {
    	this.value = optionalValue;
    }

    public static <T> Maybe<T> empty() {
        return new Maybe<T>(null);
    }

    public static <T> Maybe<T> of(T t) {
        return new Maybe<T>(t);
    }
    /**
     * Returns the value.
     * 
     * @return the value
     */
    public T get() {
        return this.value;
    }

    /**
     * Returns true if the value is not null, false otherwise.
     * 
     * @return true if the value is not null, false otherwise
     */
    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        //noinspection unchecked
        Maybe<T> other = (Maybe) obj;

        return (isPresent() == other.isPresent()) && (!isPresent() || this.value.equals(other.value));
    }

    @Override
    public int hashCode() {
        if (!isPresent()) {
            return 0;
        }
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        if (!isPresent()) {
            return "empty maybe";
        }
        return "maybe[" + this.value.toString() + "]";
    }

}