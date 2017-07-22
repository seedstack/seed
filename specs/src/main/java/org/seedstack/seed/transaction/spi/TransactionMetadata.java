/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds transaction metadata attributes.
 */
public class TransactionMetadata {
    @Inject
    @Named("default")
    @Nullable
    private Class<? extends TransactionHandler> defaultTransactionHandler;

    private Propagation propagation;
    private Boolean readOnly;
    private Boolean rollbackOnParticipationFailure;
    private Class<? extends Exception>[] rollbackOn;
    private Class<? extends Exception>[] noRollbackFor;
    private Class<? extends TransactionHandler> handler;
    private Class<? extends ExceptionHandler> exceptionHandler;
    private String resource;
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Get the propagation of the associated transaction.
     *
     * @return the {@link Propagation} instance.
     */
    public Propagation getPropagation() {
        return propagation;
    }

    /**
     * Set the propagation of the associated transaction.
     *
     * @param propagation the {@link Propagation} instance.
     */
    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }

    /**
     * Check if the associated transaction is read-only.
     *
     * @return true if read-only, false otherwise.
     */
    public Boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Set if the associated transaction is read-only.
     *
     * @param readOnly true if read-only, false otherwise.
     */
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Check if the associated participating transaction should mark the whole transaction as rollback-only.
     *
     * @return true if it marks it as rollback-only, false otherwise.
     */
    public Boolean isRollbackOnParticipationFailure() {
        return rollbackOnParticipationFailure;
    }

    /**
     * Set if the associated participating transaction should mark the whole transaction as rollback-only.
     *
     * @param rollbackOnParticipationFailure true if it marks it as rollback-only, false otherwise.
     */
    public void setRollbackOnParticipationFailure(Boolean rollbackOnParticipationFailure) {
        this.rollbackOnParticipationFailure = rollbackOnParticipationFailure;
    }

    /**
     * Get the exception classes on which the associated transaction will be rollbacked.
     *
     * @return the exception classes array.
     */
    @SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification = "Null is used here to denote an undetermined value")
    public Class<? extends Exception>[] getRollbackOn() {
        return rollbackOn != null ? rollbackOn.clone() : null;
    }

    /**
     * Set the exception classes on which the associated transaction will be rollbacked.
     *
     * @param rollbackOn the exception classes array.
     */
    public void setRollbackOn(Class<? extends Exception>[] rollbackOn) {
        this.rollbackOn = rollbackOn.clone();
    }

    /**
     * Get the exception classes on which the associated transaction will NOT be rollbacked.
     *
     * @return the exception classes array.
     */
    @SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification = "Null is used here to denote an undetermined value")
    public Class<? extends Exception>[] getNoRollbackFor() {
        return noRollbackFor != null ? noRollbackFor.clone() : null;
    }

    /**
     * Set the exception classes on which the associated transaction will NOT be rollbacked.
     *
     * @param noRollbackFor the exception classes array.
     */
    public void setNoRollbackFor(Class<? extends Exception>[] noRollbackFor) {
        this.noRollbackFor = noRollbackFor.clone();
    }

    /**
     * Get the transaction handler of the associated transaction.
     *
     * @return the transaction handler class.
     */
    public Class<? extends TransactionHandler> getHandler() {
        return handler;
    }

    /**
     * Set the transaction handler of the associated transaction.
     *
     * @param handler the transaction handler class.
     */
    public void setHandler(Class<? extends TransactionHandler> handler) {
        this.handler = handler;
    }

    /**
     * Get the name of the transacted resource (must be unique per transaction handler).
     *
     * @return the name of the transacted resource.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Set the name of the transacted resource (must be unique per transaction handler).
     *
     * @param resource the name of the transacted resource.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Get the exception handler of the associated transaction.
     *
     * @return the exception handler class.
     */
    public Class<? extends ExceptionHandler> getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Set the exception handler of the associated transaction.
     *
     * @param exceptionHandler the exception handler class.
     */
    public void setExceptionHandler(Class<? extends ExceptionHandler> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Get additional metadata.
     *
     * @param key the key of the metadata to retrieve.
     * @return the value of the metadata.
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Add additional metadata.
     *
     * @param key   the key of the metadata to add.
     * @param value the value of the metadata to add.
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Set or reset this instance to the defaults.
     *
     * @return this instance, (re-)set to the defaults.
     */
    @SuppressWarnings("unchecked")
    public TransactionMetadata defaults() {
        this.propagation = Propagation.REQUIRED;
        this.readOnly = false;
        this.rollbackOnParticipationFailure = true;
        this.rollbackOn = new Class[]{Exception.class};
        this.noRollbackFor = new Class[]{};
        this.handler = defaultTransactionHandler;
        this.exceptionHandler = null;
        this.resource = null;

        return this;
    }

    /**
     * Merge this instance with another one, which has precedence (i.e. every non null attribute will override the
     * corresponding one on this instance).
     *
     * @param other the instance to merge from.
     * @return this instance, merged.
     */
    public TransactionMetadata mergeFrom(TransactionMetadata other) {
        if (other != null) {
            if (other.propagation != null) {
                this.propagation = other.propagation;
            }
            if (other.readOnly != null) {
                this.readOnly = other.readOnly;
            }
            if (other.rollbackOnParticipationFailure != null) {
                this.rollbackOnParticipationFailure = other.rollbackOnParticipationFailure;
            }
            if (other.rollbackOn != null) {
                this.rollbackOn = other.rollbackOn;
            }
            if (other.noRollbackFor != null) {
                this.noRollbackFor = other.noRollbackFor;
            }
            if (other.handler != null) {
                this.handler = other.handler;
            }
            if (other.exceptionHandler != null) {
                this.exceptionHandler = other.exceptionHandler;
            }
            if (other.resource != null) {
                this.resource = other.resource;
            }
            this.metadata.putAll(other.metadata);
        }

        return this;
    }

    /**
     * Merge this instance with metadata defined in a {@link Transactional} annotation,
     * which has precedence over this instance.
     *
     * @param other the annotation to merge from.
     * @return this instance, merged.
     */
    public TransactionMetadata mergeFrom(Transactional other) {
        if (other != null) {
            if (other.propagation().length > 0) {
                this.propagation = other.propagation()[0];
            }
            if (other.readOnly().length > 0) {
                this.readOnly = other.readOnly()[0];
            }
            if (other.rollbackOnParticipationFailure().length > 0) {
                this.rollbackOnParticipationFailure = other.rollbackOnParticipationFailure()[0];
            }
            if (other.rollbackOn().length > 0) {
                this.rollbackOn = other.rollbackOn();
            }
            if (other.noRollbackFor().length > 0) {
                this.noRollbackFor = other.noRollbackFor();
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "transaction metadata {" +
                "propagation=" + propagation +
                ", readOnly=" + readOnly +
                ", rollbackOnParticipationFailure=" + rollbackOnParticipationFailure +
                ", rollbackOn=" + Arrays.toString(rollbackOn) +
                ", noRollbackFor=" + Arrays.toString(noRollbackFor) +
                ", handler=" + handler +
                ", exceptionHandler=" + exceptionHandler +
                ", resource='" + resource + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
