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
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpsConfigurator, HttpsParameters, HttpsServer}
import javax.net.ssl.{KeyManagerFactory, SSLContext, SSLParameters}
import java.io.{OutputStream, IOException}
import java.net.InetSocketAddress
import java.security.{KeyStore, SecureRandom}
import java.security.cert.X509Certificate
import java.util.Date
import java.math.BigInteger
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.X500Name

object SimpleHttpsServerDynamic {
  def main(args: Array[String]): Unit = {
    // --- Step 1: Generate a self-signed certificate at runtime ---
    val keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null)
    keyGen.generate(2048)
    // Validity period: 1 year (in seconds)
    val validity: Long = 365L * 24 * 60 * 60
    val x500Name = new X500Name("CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=US")
    val cert: X509Certificate = keyGen.getSelfCertificate(x500Name, new Date(), validity)

    // --- Step 2: Create an in-memory keystore and store the certificate ---
    val ks = KeyStore.getInstance("JKS")
    ks.load(null, null)
    // Generate a temporary random password (avoids inline hard-coded passwords)
    val randomPassword = new BigInteger(130, new SecureRandom()).toString(32)
    val password = randomPassword.toCharArray
    ks.setKeyEntry("selfsigned", keyGen.getPrivateKey, password, Array(cert))

    // --- Step 3: Set up SSL context using the in-memory keystore ---
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(ks, password)
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.getKeyManagers, null, null)

    // --- Step 4: Create and configure the HTTPS server ---
    val httpsPort = 8000
    val httpsServer = HttpsServer.create(new InetSocketAddress(httpsPort), 0)
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
      override def configure(params: HttpsParameters): Unit = {
        val sslParams: SSLParameters = sslContext.getDefaultSSLParameters
        params.setSSLParameters(sslParams)
      }
    })

    // Create a context and define a simple handler.
    httpsServer.createContext("/", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        val response = "Hello, HTTPS with runtime-generated self-signed certificate!"
        val responseBytes = response.getBytes("UTF-8")
        exchange.sendResponseHeaders(200, responseBytes.length)
        val os: OutputStream = exchange.getResponseBody
        try {
          os.write(responseBytes)
        } finally {
          os.close()
        }
      }
    })

    httpsServer.setExecutor(null) // Use the default executor
    httpsServer.start()

    println(s"HTTPS server started on port $httpsPort")
  }
}
```
