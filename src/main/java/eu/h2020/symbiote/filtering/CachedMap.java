package eu.h2020.symbiote.filtering;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Szymon Mueller on 03/11/2017.
 */
public class CachedMap<K, T> {

    private static final Log log = LogFactory.getLog(CachedMap.class);

//    private static final long TIME_TO_LIVE = 10 * 1000;
//    private static final long CLEANUP_INTERVAL = 120 * 1000; //Every 2 minutes

    private final long ttl;
    private final long cleanupInterval;

    private LRUMap securityCache;

    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected CacheObject(T value) {
            this.value = value;
        }
    }

    public CachedMap(long ttl, long cleanupInterval, int maxItems) {

        this.ttl = ttl;
        this.cleanupInterval = cleanupInterval;

        securityCache = new LRUMap(maxItems);

        if (ttl > 0 && cleanupInterval > 0) {

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    cleanup();
                }
            };

            Timer timer = new Timer("SecurityCacheCleanup",true);
            timer.schedule(task,cleanupInterval,cleanupInterval);
        }
    }

    public void put(K key, T value) {
        synchronized (securityCache) {
            securityCache.put(key, new CacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (securityCache) {
            CacheObject c = (CacheObject) securityCache.get(key);

            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    public void remove(K key) {
        synchronized (securityCache) {
            securityCache.remove(key);
        }
    }

    public int size() {
        synchronized (securityCache) {
            return securityCache.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (securityCache) {
            MapIterator itr = securityCache.mapIterator();

            deleteKey = new ArrayList<K>((securityCache.size() / 2) + 1);
            K key = null;
            CacheObject c = null;

            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();

                if (c != null && (now > (ttl + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            synchronized (securityCache) {
                securityCache.remove(key);
            }
        }
    }
}
