# Declarative NATS listeners starter for Spring Boot

NATS is a technology for asynchronous message exchange between various parts of a distributed system. It can be used
both as a
message broker and a streaming solution. This starter aims to provide an easy-to-use integration layer between a Spring
Boot application and core NATS client libraries. It follows a declarative API pattern modelled after Spring for Apache
Kafka project, i.e. you get your infrastructure beans autoconfigured, and you use annotations to declare listener
methods.

## TLDR;

First, add a dependency to your pom.xml:

```xml

<dependency>
 <groupId>org.amalnev</groupId>
 <artifactId>declarative-nats-listeners-starter</artifactId>
 <version>0.1</version>
</dependency>
```

The artifact is hosted on GitHub itself, right
here: https://github.com/amalnev/declarative-nats-listeners/packages/1701966/

Please refer to GitHub documentation on how to configure Maven to use GitHub as a package
repository: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry

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

## Configuring connection details

Connection details can be configured in application.yaml or application.properties. A full set of supported properties
is this:

```yaml
nats:
 bootstrap-servers: "nats://localhost:4222,nats://localhost:5222"
 username-password-auth:
  enabled: true
  username: user
  password: pass
 n-key-auth:
  enabled: true
  n-key-seed: NKEYSEEDDATA
 use-tls: true
```

The only required property is ```nats.bootstrap-servers```. It is possible to have several bootstrap servers for a NATS
cluster - separate them with comma as shown above.

If your servers use traditional username/password authentication, set ```nats.username-password-auth.enabled```
to ```true``` and provide valid credentials.

NATS recommends to use so called NKey authentication process, as described
here https://docs.nats.io/running-a-nats-service/configuration/securing_nats/auth_intro/nkey_auth. If that is the case,
set ```nats.n-key-auth.enabled``` to ```true``` and provide valid data in ```nats.n-key-auth.n-key-seed```

If both username/password and NKey auth are enabled, then NKey authentication method will take precedence, so actually
there is no point in having them both enabled. Enable one of the methods and disable the other to reflect your server
setup. If both authentication methods are disabled or simply not configured in application.yaml, then an attempt will be
made to connect without authentication.

If your servers use TLS for transport, set ```nats.use-tls``` to ```true```.

## Receiving messages

NATS can basically operate in 2 modes: as a message broker following the traditional publish/subscribe model (so called
Core NATS) and as a streaming platform with its built-in distributed persistence system called JetStream. With JetStream
enabled, NATS works similar to Apache Kafka - you can replay stored messages by setting the current offset to an
arbitrary position in the stream.

Consequently, we have 2 options to declare listener methods: ```@NatsListener``` and ```@JetStreamListener```. The first
one can be used with both "normal" subjects and subjects backed by streams. The second one can be used only with streams
and additionally allows to specify which messages from the stream are to be delivered according to "delivery policy" (
more on that can be found here https://docs.nats.io/nats-concepts/jetstream/consumers#deliverpolicy).

### @NatsListener

Let's look at the example

```java

@Component
class NatsListeners {

 @NatsListener(
         subject = "my.subject",
         queue = "queue-1",
         concurrency = 2)
 public void handleMessage(Message msg) {
  //...
 }
}
```

Important things to note here are:

- Listener methods (both ```@NatsListener```s and ```@JetStreamListener```s) can only be declared in Spring beans
- Listener methods (both ```@NatsListener```s and ```@JetStreamListener```s) must be public, must return void and take a
  single parameter of type ```io.nats.client.Message```
- The only required parameter of ```@NatsListener``` annotation is ```subject```. If the value for ```queue``` is not
  specified, it will be randomly and uniquely generated at runtime. If the value for ```concurrency``` is not specified,
  the default (1) will be used
- ```queue``` is analogous to consumer group id in Apache Kafka. If 2 listeners belong to the same queue they will
  receive only one copy of each message published to the corresponding subject. If 2 listeners belong to different
  queues they will each get their own
  copy of each message published to the corresponding subject.
- ```concurrency``` indicates the number of threads that will concurrently call the corresponding listener method when
  new messages arrive

### @JetStreamListener

```java

@Component
class NatsListeners {

 @JetStreamListener(
         subject = "my.subject",
         queue = "queue-1",
         deliverPolicy = "DeliverAll",
         concurrency = 2)
 public void handleMessage(Message msg) {
  //...
 }
}
```

```@JetStreamListener```s work basically the same way as regular ```@NatsListener```s with the following important
exceptions:

- ```@JetStreamListener```s can only be used with streams
- ```@JetStreamListener``` has an additional parameter - ```deliverPolicy``` - that specifies which messages from the
  stream are to be delivered. Possible values and their meaning are explained
  here: https://docs.nats.io/nats-concepts/jetstream/consumers#deliverpolicy
- ```queue``` in ```@JetStreamListener``` works by creating a durable push consumer on a server, so it is better to
  specify the value for this parameter explicitly. Otherwise, a unique random value will be generated every time
  application starts, and we can end up with too many unused consumers on the server.

### Examples

There is an example Spring Boot application that uses the
starter: https://github.com/amalnev/declarative-nats-listeners/tree/master/declarative-nats-listeners-test-app