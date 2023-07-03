package io.github.nathanadhitya.velocityrediswhitelist;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.annotations.Expose;

import java.net.URL;
import java.nio.file.Path;

public class VelocityRedisWhitelistConfig {
    @Expose
    private final String redisUri;
    @Expose
    private final String notWhitelistedMessage;

    public VelocityRedisWhitelistConfig(String redisUri, String notWhitelistedMessage) {
        this.redisUri = redisUri;
        this.notWhitelistedMessage = notWhitelistedMessage;
    }

    public static VelocityRedisWhitelistConfig read(Path path) {
        URL defaultConfigLocation = VelocityRedisWhitelistConfig.class.getClassLoader().getResource("vrw.toml");

        if (defaultConfigLocation == null) {
            throw new RuntimeException("Default config not found!");
        }

        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .defaultData(defaultConfigLocation)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        config.load();

        String redisUri = config.getOrElse("redisUri", "redis://localhost:6379");
        String notWhitelistedMessage = config.getOrElse("notWhitelistedMessage", "You are not whitelisted!");

        return new VelocityRedisWhitelistConfig(redisUri, notWhitelistedMessage);
    }

    public String getRedisUri() {
        return redisUri;
    }

    public String getNotWhitelistedMessage() {
        return notWhitelistedMessage;
    }
}
