package eu.h2020.symbiote.filtering;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Szymon Mueller on 03/11/2017.
 */
public class SecurityCache<K, T> {

    private static final long TIME_TO_LIVE = 10 * 1000;
    private static final long CLEANUP_INTERVAL = 600 * 1000; //Every 10 minutes
    private LRUMap securityCache;

    protected class SecurityCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected SecurityCacheObject(T value) {
            this.value = value;
        }
    }

    public SecurityCache(int maxItems) {

        securityCache = new LRUMap(maxItems);

        if (TIME_TO_LIVE > 0 && CLEANUP_INTERVAL > 0) {

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    cleanup();
                }
            };

            Timer timer = new Timer("SecurityCacheCleanup",true);
            timer.schedule(task,CLEANUP_INTERVAL,CLEANUP_INTERVAL);
        }
    }

    public void put(K key, T value) {
        synchronized (securityCache) {
            securityCache.put(key, new SecurityCacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (securityCache) {
            SecurityCacheObject c = (SecurityCacheObject) securityCache.get(key);

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
            SecurityCacheObject c = null;

            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (SecurityCacheObject) itr.getValue();

                if (c != null && (now > (TIME_TO_LIVE + c.lastAccessed))) {
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
