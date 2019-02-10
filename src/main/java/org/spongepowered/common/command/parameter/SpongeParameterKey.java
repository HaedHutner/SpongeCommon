package org.spongepowered.common.command.parameter;

import org.spongepowered.api.command.parameter.Parameter;

import java.util.Objects;

public final class SpongeParameterKey<T> implements Parameter.Key<T> {

    private final String key;
    private final Class<T> clazz;

    public static <T> SpongeParameterKey<T> getSpongeKey(Parameter.Key<T> key) {
        if (key instanceof SpongeParameterKey) {
            return (SpongeParameterKey<T>) key;
        }

        return new SpongeParameterKey<>(key);
    }

    public SpongeParameterKey(Parameter.Key<T> parameterKey) {
        this(parameterKey.key(), parameterKey.getValueClass());
    }

    public SpongeParameterKey(String key, Class<T> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Class<T> getValueClass() {
        return this.clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeParameterKey<?> that = (SpongeParameterKey<?>) o;
        return this.key.equals(that.key) && this.clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.clazz);
    }

    public static class Builder implements Parameter.Key.Builder {

        @Override
        public <T> Parameter.Key<T> build(String key, Class<T> valueClass) {
            return new SpongeParameterKey<>(key, valueClass);
        }

        @Override
        public Parameter.Key.Builder reset() {
            return this;
        }

    }

}
