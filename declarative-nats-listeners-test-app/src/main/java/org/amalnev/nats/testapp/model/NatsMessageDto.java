package org.amalnev.nats.testapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NatsMessageDto {

    @JsonProperty("string_field")
    public String stringField;

    @JsonProperty("integer_field")
    public Integer integerField;
}
