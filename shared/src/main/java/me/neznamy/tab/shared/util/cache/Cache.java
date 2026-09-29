package me.neznamy.tab.shared.util.cache;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache to save resources when converting the same values over and over.
 * When the size limit is reached, only the least recently used entry is evicted instead of
 * clearing the whole cache, which would force all values to be converted again at once
 * (and break identity-based change detection of converted values).
 *
 * @param   <K>
 *          Source to convert from
 * @param   <V>
 *          Target to convert to
 */
public abstract class Cache<K, V> {

    /** Name of this cache for debugging purposes */
    @Getter
    private final String name;

    /** Cached values in access order */
    private final Map<K, V> cache;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   name
     *          Cache name
     * @param   cacheSize
     *          Maximum amount of cached values
     */
    protected Cache(@NotNull String name, int cacheSize) {
        this.name = name;
        cache = new LinkedHashMap<K, V>(Math.min(cacheSize, 256), 0.75f, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > cacheSize;
            }
        };
    }

    /**
     * Gets value from cache. If not present, it is created using given function, inserted
     * into the cache and then returned.
     *
     * @param   key
     *          Source to convert
     * @return  Converted value
     */
    @NotNull
    public synchronized V get(@NotNull K key) {
        V value = cache.get(key);
        if (value == null) {
            value = convert(key);
            cache.put(key, value);
        }
        return value;
    }

    /**
     * Converts source to target type.
     *
     * @param   key
     *          Source to convert
     * @return  Converted value
     */
    @NotNull
    public abstract V convert(@NotNull K key);
}
