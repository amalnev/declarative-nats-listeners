package org.amalnev.nats.testapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;

@Data
@Accessors(chain = true)
public class NatsMessageDto implements Cloneable {

    @JsonProperty("string_field")
    public String stringField;

    @JsonProperty("integer_field")
    public Integer integerField;

    @Override
    @SneakyThrows
    public NatsMessageDto clone() {
        return (NatsMessageDto) super.clone();
    }

    @SneakyThrows
    public static NatsMessageDto fromNatsMessage(Message natsMessage, ObjectMapper objectMapper) {
        String messagePayloadAsString = new String(natsMessage.getData(), StandardCharsets.UTF_8);
        return objectMapper.readValue(
                messagePayloadAsString,
                NatsMessageDto.class);
    }
}
