///usr/bin/env jbang "$0" "$@" ; exit $?

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

class CoreHttp {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, InvalidKeySpecException {
        Path pathToKey = Path.of(args[0]);
        Path pathToCert = Path.of(args[1]);
        String uri = args[2];

        // Configure a key store with the client key and certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        byte[] keyBytes = Files.readAllBytes(pathToKey);
        PrivateKey clientKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        Certificate[] clientCertificates = CertificateFactory
                .getInstance("X.509")
                .generateCertificates(Files.newInputStream(pathToCert))
                .toArray(new Certificate[0]);

        keyStore
                .setEntry("clientCert", new KeyStore.PrivateKeyEntry(clientKey, clientCertificates), new KeyStore.PasswordProtection(new char[0]));

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, null);

        // Create an HTTP client with a custom SSL context
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();

        // Make request
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).GET().build();
        httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }
}
