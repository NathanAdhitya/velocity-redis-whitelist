package io.github.nathanadhitya.velocityrediswhitelist;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PlayerJoinListener {
    private final VelocityRedisWhitelist plugin;

    public PlayerJoinListener(VelocityRedisWhitelist plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        try {
            // Get Jedis Pool
            JedisPool jedisPool = plugin.getJedisPool();
            Jedis jedis = jedisPool.getResource();

            boolean isPlayerWhitelisted = jedis.sismember("whitelist.main", event.getPlayer().getUniqueId().toString());
            if (!isPlayerWhitelisted) {
                event.setResult(
                    ResultedEvent.ComponentResult.denied(
                        Component.text(
                            plugin.getConfig()
                                .getNotWhitelistedMessage(),
                            NamedTextColor.RED
                        )
                    )
                );
            } else {
                event.setResult(ResultedEvent.ComponentResult.allowed());
            }
        } catch (Exception e) {
            event.setResult(ResultedEvent.ComponentResult.denied(
                    Component.text(
                        "An unknown error occurred. Please try again. Contact server admin if error persists.",
                        NamedTextColor.RED
                    )
                )
            );

            plugin.getLogger().error("An error occurred while checking if player is whitelisted: ", e);
        }
    }
}
