package io.github.nathanadhitya.velocityrediswhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
    id = "velocity-redis-whitelist",
    name = "Velocity Redis Whitelist",
    version = "0.1.0-SNAPSHOT",
    url = "https://github.com/NathanAdhitya/velocity-redis-whitelist",
    description = "Redis-based UUID whitelisting for Velocity",
    authors = {
        "NathanAdhitya"
    }
)
public class VelocityRedisWhitelist {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private JedisPool jedisPool;
    private final JedisPoolConfig jedisPoolConfig;
    private VelocityRedisWhitelistConfig config;

    @Inject
    public VelocityRedisWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(64);
        jedisPoolConfig.setMaxIdle(64);
        jedisPoolConfig.setMinIdle(2);
        jedisPoolConfig.setTestWhileIdle(true);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Load the config
        reloadConfig();

        // Register the event listener
        server.getEventManager().register(this, new PlayerJoinListener(this));

        // Register the command
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("vrw").aliases("whitelist").plugin(this).build();
        server.getCommandManager().register(commandMeta, new VRWCommand(this));
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        // Reload the config
        reloadConfig();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // Close the Jedis pool
        jedisPool.close();
    }

    public void reloadConfig() {
        try {
            Files.createDirectories(dataDirectory);
            config = VelocityRedisWhitelistConfig.read(dataDirectory.resolve("config.toml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Replace the Jedis pool
        try {
            JedisPool newPool = new JedisPool(jedisPoolConfig, new URI(config.getRedisUri()));
            JedisPool oldPool = jedisPool;
            jedisPool = newPool;
            if (oldPool != null && !oldPool.isClosed())
                oldPool.close();
        } catch (Exception e) {
            logger.error("An error occurred while creating a new Jedis pool: ", e);
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public VelocityRedisWhitelistConfig getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }
}