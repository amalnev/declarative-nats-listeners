package org.amalnev.nats.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nats")
public class NatsConfigurationProperties {

    @NotBlank
    private String bootstrapServers;

    @Valid
    private UsernamePasswordAuthenticationProperties usernamePasswordAuth;

    @Valid
    private NKeyAuthenticationProperties nKeyAuth;

    private Boolean useTls = false;

    @Data
    public static class UsernamePasswordAuthenticationProperties {

        @NotNull
        private Boolean enabled;

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Data
    public static class NKeyAuthenticationProperties {

        @NotNull
        private Boolean enabled;

        @NotBlank
        private String nKeySeed;
    }
}
