package me.hippo.api.lwjeb.extensions.rle;


import com.google.gson.Gson;
import me.hippo.api.lwjeb.bus.AbstractAsynchronousPubSubMessageBus;
import me.hippo.api.lwjeb.configuration.config.impl.ExceptionHandlingConfiguration;
import me.hippo.api.lwjeb.extensions.rle.util.SerializationUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Hippo
 * @version 1.0.0, 4/21/20
 * @since 1.0.0
 *
 * A <tt>Redis Pub Sub</tt> is an implementation around {@link JedisPubSub}.
 * It wraps around and hooks into {@link AbstractAsynchronousPubSubMessageBus} for event publication.
 */
public final class RedisPubSub extends JedisPubSub {

    /**
     * The parent bus.
     * <p>
     *     The <tt>result queue</tt> of this bus will be modified.
     * </p>
     */
    private final AbstractAsynchronousPubSubMessageBus<Serializable> parentBus;

    /**
     * The <tt>threads</tt> that subscribe to a redis channel.
     */
    private final List<Thread> jedisChannelThreads;

    /**
     * If the bus should be shut down.
     */
    private final AtomicBoolean shutdown;

    /**
     * The current {@link Gson} instance for serialization.
     */
    private Gson gson;

    /**
     * Constructs a <tt>Redis Pub Sub</tt> with the desired <tt>parent bus</tt>.
     *
     * @param parentBus  The parent bus.
     */
    public RedisPubSub(AbstractAsynchronousPubSubMessageBus<Serializable> parentBus) {
        this.parentBus = parentBus;
        this.jedisChannelThreads = new LinkedList<>();
        this.shutdown = new AtomicBoolean(true);
        this.gson = new Gson();
    }

    /**
     * Whenever a message is received from the the redis pubsub channel
     * that message will be deserialized into a <tt>topic</tt> and added into the
     * <tt>parent bus</tt>'s result queue.
     *
     * @param channel  The channel.
     * @param message  The message
     */
    @Override
    public void onMessage(String channel, String message) {
        try {
            parentBus.addMessage(parentBus.getPublisher().publish(SerializationUtil.deserialize(message, gson), parentBus));
        }catch (ClassNotFoundException e) {
            parentBus.getConfigurations().get(ExceptionHandlingConfiguration.class).getExceptionHandler().handleException(e);
        }
    }

    /**
     * Post a <tt>topic</tt> to the default redis channel.
     *
     * @param jedisPool  The jedis pool.
     * @param topic  The topic.
     */
    public void post(JedisPool jedisPool, Serializable topic) {
        post(jedisPool, "LWJEB", topic);
    }

    /**
     * Post a <tt>topic</tt> to the <tt>channel</tt> redis channel.
     *
     * @param jedisPool  The jedis pool.
     * @param channel  The channel.
     * @param topic  the topic.
     */
    public void post(JedisPool jedisPool, String channel, Serializable topic) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, SerializationUtil.serialize(topic, gson));
        }
    }

    /**
     * Subscribes to the default redis channel.
     *
     * @param jedisPool  The jedis pool.
     */
    public void subscribeRedisChannel(JedisPool jedisPool) {
        subscribeRedisChannel(jedisPool, "LWJEB");
    }

    /**
     * Subscribes to the <tt>channel</tt> redis channel.
     *
     * @param jedisPool
     * @param channel
     */
    public void subscribeRedisChannel(JedisPool jedisPool, String channel) {
        Thread thread = new Thread(() -> {
            while (shutdown.get()) {
                Jedis jedis = jedisPool.getResource();
                jedis.subscribe(this, channel);
                jedis.close();
            }
        }, parentBus.getIdentifier() + " - Redis Channel (" + channel + ")");
        thread.start();
        jedisChannelThreads.add(thread);
    }

    /**
     * Shuts down this bus and the <tt>parent bus</tt>.
     */
    public void shutdown() {
        shutdown.set(false);
        for (Thread jedisChannelThread : jedisChannelThreads) {
            jedisChannelThread.interrupt();
        }
        parentBus.shutdown();
    }

    /**
     * Gets the <tt>parent bus</tt>.
     *
     * @return  The parent bus.
     */
    public AbstractAsynchronousPubSubMessageBus<Serializable> getParentBus() {
        return parentBus;
    }

    /**
     * Gets the gson instance.
     *
     * @return  The gson.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Sets the gson instance.
     *
     * @param gson  The gson.
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
