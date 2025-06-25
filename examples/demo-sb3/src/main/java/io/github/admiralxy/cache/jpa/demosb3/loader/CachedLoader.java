package io.github.admiralxy.cache.jpa.demosb3.loader;

@FunctionalInterface
public interface CachedLoader {
    void loadValue(String id);
}
