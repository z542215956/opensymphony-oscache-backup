/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.general;

import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.Properties;

/**
 * A GeneralCacheAdministrator creates, flushes and administers the cache.
 *
 * EXAMPLES :
 * <pre><code>
 *                // ---------------------------------------------------------------
 *                // Typical use with fail over
 *                // ---------------------------------------------------------------
 *    String myKey = "myKey";
 *    String myValue;
 *    int myRefreshPeriod = 1000;
 *                try
 *                {
 *                        // Get from the cache
 *                        myValue = (String) admin.getFromCache(myKey, myRefreshPeriod);
 *                }
 *                catch (NeedsRefreshException nre)
 *                {
 *                        try
 *                        {
 *                                // Get the value (probably by calling an EJB)
 *                                myValue = "This is the content retrieved.";
 *                                // Store in the cache
 *                                admin.putInCache(myKey,        myValue);
 *                        }
 *                        catch (Exception ex)
 *                        {
 *                                // We have the current content if we want fail-over.
 *                                myValue = (String) nre.getCacheContent();
 *                        }
 *                }
 *                // ---------------------------------------------------------------
 *                // ---------------------------------------------------------------
 *
 *                // ---------------------------------------------------------------
 *                // Typical use without fail over
 *                // ---------------------------------------------------------------
 *      String myKey = "myKey";
 *                String myValue;
 *      int myRefreshPeriod = 1000;
 *                try
 *                {
 *                        // Get from the cache
 *                        myValue = (String) admin.getFromCache(myKey, myRefreshPeriod);
 *                }
 *                catch (NeedsRefreshException nre)
 *                {
 *                        // Get the value (probably by calling an EJB)
 *                        myValue = "This is the content retrieved.";
 *                        // Store in the cache
 *                        admin.putInCache(myKey,        myValue);
 *                }
 *                // ---------------------------------------------------------------
 *                // ---------------------------------------------------------------
 * </code></pre>
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class GeneralCacheAdministrator extends AbstractCacheAdministrator {
    private static transient final Log log = LogFactory.getLog(GeneralCacheAdministrator.class);

    /**
     * Application cache
     */
    private Cache applicationCache = null;

    /**
     * Create the cache administrator.
     */
    public GeneralCacheAdministrator() {
        this(null);
    }

    /**
     * Create the cache administrator with the specified properties
     */
    public GeneralCacheAdministrator(Properties p) {
        super(p);
        log.info("Constructed GeneralCacheAdministrator()");
    }

    /**
     * Grabs a cache
     *
     * @return The cache
     */
    public Cache getCache() {
        // Create the cache if it doesn't already exist
        if (applicationCache == null) {
            applicationCache = createCache();
        }

        return applicationCache;
    }

    /**
     * Get an object from the cache
     *
     * @param key             The key entered by the user.
     * @param refreshPeriod   How long the object can stay in cache in seconds.
     * @return   The object from cache
     * @throws NeedsRefreshException when no cache entry could be found with the
     * supplied key, or when an entry was found but is considered out of date. If
     * the cache entry is a new entry that is currently being constructed this method
     * will block until the new entry becomes available. Similarly, it will block if
     * a stale entry is currently being rebuilt by another thread and cache blocking is
     * enabled (<code>cache.blocking=true</code>).
     */
    public Object getFromCache(String key, int refreshPeriod) throws NeedsRefreshException {
        return getCache().getFromCache(key, refreshPeriod);
    }

    /**
     * Get an object from the cache
     *
     * @param key             The key entered by the user.
     * @param refreshPeriod   How long the object can stay in cache in seconds.
     * @param cronExpression  A cron expression that the age of the cache entry
     * will be compared to. If the entry is older than the most recent match for the
     * cron expression, the entry will be considered stale.
     * @return   The object from cache
     * @throws NeedsRefreshException when no cache entry could be found with the
     * supplied key, or when an entry was found but is considered out of date. If
     * the cache entry is a new entry that is currently being constructed this method
     * will block until the new entry becomes available. Similarly, it will block if
     * a stale entry is currently being rebuilt by another thread and cache blocking is
     * enabled (<code>cache.blocking=true</code>).
     */
    public Object getFromCache(String key, int refreshPeriod, String cronExpression) throws NeedsRefreshException {
        return getCache().getFromCache(key, refreshPeriod, cronExpression);
    }

    /**
     * Cancels a pending cache update. This should only be called by a thread
     * that received a {@link NeedsRefreshException} and was unable to generate
     * some new cache content.
     *
     * @param key The cache entry key to cancel the update of.
     */
    public void cancelUpdate(String key) {
        getCache().cancelUpdate(key);
    }

    /**
     * Shuts down the cache administrator.
     */
    public void destroy() {
        finalizeListeners(applicationCache);
    }

    // METHODS THAT DELEGATES TO THE CACHE ---------------------

    /**
     *        Flush all the cache at a given date
     *
     *        @param date         The time to flush
     */
    public void flushAll(Date date) {
        getCache().flushAll(date);
    }

    /**
     * Flushes all items that belong to the specified group.
     *
     * @param group The name of the group to flush
     */
    public void flushGroup(String group) {
        getCache().flushGroup(group);
    }

    /**
     * Allows to flush all items that have a specified pattern in the key.
     *
     * @param pattern     Pattern.
     */
    public void flushPattern(String pattern) {
        getCache().flushPattern(pattern);
    }

    /**
     * Put an object in a cache
     *
     * @param key       The key entered by the user
     * @param content   The object to store
     * @param policy    Object that implements refresh policy logic
     */
    public void putInCache(String key, Object content, EntryRefreshPolicy policy) {
        Cache cache = getCache();
        cache.putInCache(key, content, policy);
    }

    /**
     * Put an object in a cache
     *
     * @param key       The key entered by the user
     * @param content   The object to store
     */
    public void putInCache(String key, Object content) {
        putInCache(key, content, (EntryRefreshPolicy) null);
    }

    /**
     * Puts an object in a cache
     *
     * @param key      The unique key for this cached object
     * @param content  The object to store
     * @param groups   The groups that this object belongs to
     */
    public void putInCache(String key, Object content, String[] groups) {
        getCache().putInCache(key, content, groups);
    }

    /**
     * Puts an object in a cache
     *
     * @param key      The unique key for this cached object
     * @param content  The object to store
     * @param groups   The groups that this object belongs to
     * @param policy   The refresh policy to use
     */
    public void putInCache(String key, Object content, String[] groups, EntryRefreshPolicy policy) {
        getCache().putInCache(key, content, groups, policy, null);
    }

    /**
     * Creates a cache in this admin
     *
     * @return The new cache
     */
    private Cache createCache() {
        log.info("Creating new cache");

        Cache newCache = new Cache(isMemoryCaching(), isUnlimitedDiskCache(), isBlocking(), algorithmClass, cacheCapacity);

        // Give the cache a name
        nameCache("applicationCache", newCache);

        configureStandardListeners(newCache);

        return newCache;
    }
}