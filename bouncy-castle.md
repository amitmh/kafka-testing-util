
```scala
import java.io.{FileReader, FileInputStream, BufferedReader, OutputStream}
import java.net.InetSocketAddress
import java.security.{KeyStore, Security}
import java.security.cert.X509Certificate
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpsConfigurator, HttpsServer}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter

object BouncyCastleHttpsServer {
  def main(args: Array[String]): Unit = {
    // Register the Bouncy Castle provider if not already registered
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider)
    }

    // File paths for the PEM certificate and private key
    val certFilePath = "server.pem"
    val keyFilePath  = "server.key"

    // Load the certificate from the PEM file
    val certParser = new PEMParser(new FileReader(certFilePath))
    val certHolder = certParser.readObject()  // expected to be an X509CertificateHolder
    certParser.close()
    val certConverter = new JcaX509CertificateConverter().setProvider("BC")
    val certificate: X509Certificate = certConverter.getCertificate(certHolder.asInstanceOf[org.bouncycastle.cert.X509CertificateHolder])

    // Load the private key from the PEM file
    val keyParser = new PEMParser(new FileReader(keyFilePath))
    val keyObject = keyParser.readObject()  // expected to be a PEMKeyPair or private key object
    keyParser.close()
    val keyConverter = new JcaPEMKeyConverter().setProvider("BC")
    // If the key was stored as a key pair, extract the private key
    val privateKey = keyObject match {
      case pair: org.bouncycastle.openssl.PEMKeyPair =>
        keyConverter.getKeyPair(pair).getPrivate
      case keyObj =>
        keyConverter.getPrivateKey(keyObj.asInstanceOf[org.bouncycastle.openssl.PEMKeyPair])
    }

    // Create an in-memory KeyStore and store the private key and certificate
    val keyStorePassword = "password".toCharArray
    val keyStore = KeyStore.getInstance("PKCS12")
    keyStore.load(null, null)
    // Store the key under the alias "myserver" along with the certificate chain (in this case, just one certificate)
    keyStore.setKeyEntry("myserver", privateKey, keyStorePassword, Array(certificate))

    // Initialize the KeyManagerFactory with the KeyStore
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(keyStore, keyStorePassword)

    // Create and initialize the SSLContext using TLS
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.getKeyManagers, null, null)

    // Create the HTTPS server on port 8000
    val httpsServer = HttpsServer.create(new InetSocketAddress(8000), 0)
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext))

    // Define a simple request handler returning "Hello World"
    httpsServer.createContext("/", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        val response = "Hello World"
        exchange.sendResponseHeaders(200, response.getBytes.length)
        val os: OutputStream = exchange.getResponseBody
        os.write(response.getBytes)
        os.close()
      }
    })

    httpsServer.setExecutor(null) // uses default executor
    httpsServer.start()
    println("HTTPS server started on port 8000")
  }
}
```
