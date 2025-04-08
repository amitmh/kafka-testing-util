#### Kafka Test Util

Provides wrapper around [KafkaServerStartable](https://github.com/apache/kafka/blob/trunk/core/src/main/scala/kafka/server/KafkaServerStartable.scala)

##### Usage

The simplest way to execute `block` while making sure a kafka instance is running:

```scala 
 KafkaTestUtil().withKafkaRunning {
    // block
}
```

##### Overriding configuration

Kafka startup properties can be overwritten using constructor methods.
Note that `config` overrides default `kafkaPort` on conflict.

```
    KafkaTestUtil(kafkaPort = 9092, zookeeperPort= 2181, config = Map.empty)
```


```scala
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public class SimpleHttpsServerDynamic {
    public static void main(String[] args) throws Exception {
        // --- Step 1: Generate a self-signed certificate at runtime ---
        // Create a key pair using RSA and SHA256WithRSA signature algorithm.
        CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        keyGen.generate(2048);

        // Validity period: 1 year (in seconds)
        long validity = 365 * 24L * 60L * 60L;
        // Provide a simple X500 name (adjust details as necessary)
        X500Name x500Name = new X500Name("CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=US");
        // Generate a self-signed X.509 certificate.
        X509Certificate cert = keyGen.getSelfCertificate(x500Name, new Date(), validity);

        // --- Step 2: Create an in-memory keystore and store the certificate ---
        // We use JKS here; since the keystore is built dynamically, we can generate a random password.
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);

        // Generate a temporary (random) password so nothing is hard-coded.
        String randomPassword = new java.math.BigInteger(130, new SecureRandom()).toString(32);
        char[] password = randomPassword.toCharArray();

        // Store the generated private key and self-signed certificate in the keystore.
        ks.setKeyEntry("selfsigned", keyGen.getPrivateKey(), password, new X509Certificate[]{cert});

        // --- Step 3: Set up SSL context using the in-memory keystore ---
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // --- Step 4: Create and configure the HTTPS server ---
        int httpsPort = 8000; // Set your desired HTTPS port.
        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(httpsPort), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                // Set up SSL parameters from the context.
                SSLParameters sslParams = sslContext.getDefaultSSLParameters();
                params.setSSLParameters(sslParams);
            }
        });

        // Create a simple context and handler.
        httpsServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Hello, HTTPS with runtime-generated self-signed certificate!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        httpsServer.setExecutor(null); // Use the default executor.
        httpsServer.start();

        System.out.println("HTTPS server started on port " + httpsPort);
    }
}```
