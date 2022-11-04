package org.amalnev.nats.config;

import io.nats.client.*;
import lombok.SneakyThrows;
import org.amalnev.nats.properties.NatsConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(NatsConfigurationProperties.class)
public class NatsConnectionConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConnectionListener.class)
    public ConnectionListener connectionListener() {
        return (connection, eventType) -> {
        };
    }

    @Bean
    @ConditionalOnMissingBean(ErrorListener.class)
    public ErrorListener errorListener() {
        return new ErrorListener() {
        };
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(Connection.class)
    public Connection natsConnection(NatsConfigurationProperties configurationProperties,
                                     ConnectionListener connectionListener,
                                     ErrorListener errorListener) {
        Options.Builder connectionOptionsBuilder = new Options.Builder();
        connectionOptionsBuilder = connectionOptionsBuilder.server(configurationProperties.getBootstrapServers());

        if (usernamePasswordAuthShouldBeUsed(configurationProperties)) {
            connectionOptionsBuilder = connectionOptionsBuilder.userInfo(
                    configurationProperties.getUsernamePasswordAuth().getUsername(),
                    configurationProperties.getUsernamePasswordAuth().getPassword());
        }

        if (nKeyAuthShouldBeUsed(configurationProperties)) {
            connectionOptionsBuilder = connectionOptionsBuilder.authHandler(
                    new AuthHandler() {
                        private final NKey nKey = NKey.fromSeed(
                                configurationProperties.getNKeyAuth().getNKeySeed().toCharArray());

                        @Override
                        @SneakyThrows
                        public byte[] sign(byte[] nonce) {
                            return nKey.sign(nonce);
                        }

                        @Override
                        @SneakyThrows
                        public char[] getID() {
                            return nKey.getPublicKey();
                        }

                        @Override
                        public char[] getJWT() {
                            return null;
                        }
                    });
        }

        if (configurationProperties.getUseTls()) {
            connectionOptionsBuilder.sslContext(SSLContext.getDefault());
        }

        Options connectionOptions = connectionOptionsBuilder
                .connectionListener(connectionListener)
                .errorListener(errorListener)
                .build();
        return Nats.connect(connectionOptions);
    }

    private boolean usernamePasswordAuthShouldBeUsed(NatsConfigurationProperties configurationProperties) {
        return !nKeyAuthShouldBeUsed(configurationProperties) &&
                Objects.nonNull(configurationProperties.getUsernamePasswordAuth()) &&
                configurationProperties.getUsernamePasswordAuth().getEnabled();
    }

    private boolean nKeyAuthShouldBeUsed(NatsConfigurationProperties configurationProperties) {
        return Objects.nonNull(configurationProperties.getNKeyAuth()) &&
                configurationProperties.getNKeyAuth().getEnabled();
    }
}
