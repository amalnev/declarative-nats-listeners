# Declarative NATS listeners starter for Spring Boot

NATS is a technology for asynchronous message exchange between various parts of a distributed system. It can be used
both as a
message broker and a streaming solution. This library aims to provide an easy-to-use integration layer between a Spring
Boot application and core NATS client libraries. It follows a declarative API pattern modelled after Spring for Apache
Kafka project, i.e. you get your infrastructure beans autoconfigured, and you use annotations to declare listener
methods.

### TLDR;

First, add a dependency to your pom.xml:

```xml

<dependency>
    <groupId>org.amalnev</groupId>
    <artifactId>declarative-nats-listeners-starter</artifactId>
    <version>0.1</version>
</dependency>
```

Add some configurations to you application.yaml, at the very least you will need an address of a NATS server:

```yaml
nats:
  bootstrap-servers: "nats://localhost:4222"
```

Put @Enable* annotations over one of your @Configuration classes:

```java

@EnableNatsListeners
@EnableJetStreamListeners
@SpringBootApplication
public class NatsClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(NatsClientApplication.class, args);
    }
}
```

Now you can use @NatsListener and @JetStreamListener annotations to start receiving NATS messages:

```java

@Service
public class NatsListenerService {

    @NatsListener(subject = "my.subject")
    public void handleMessagesFromMySubject(Message natsMessage) {
        //...
    }

    @JetStreamListener(subject = "another.subject")
    public void handleMessagesFromAnotherSubject(Message natsMessage) {
        //...
    }
}
```

A more detailed description follows