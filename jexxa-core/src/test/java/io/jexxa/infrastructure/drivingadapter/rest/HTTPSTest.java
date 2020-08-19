package io.jexxa.infrastructure.drivingadapter.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import io.jexxa.application.applicationservice.SimpleApplicationService;
import kong.unirest.Unirest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HTTPSTest
{
    private static final String REST_PATH_HTTPS = "https://localhost:8080/SimpleApplicationService/";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_TYPE = "application/json";
    private static final String METHOD_GET_SIMPLE_VALUE = "getSimpleValue";

    private static final int DEFAULT_VALUE = 42;
    private final SimpleApplicationService simpleApplicationService = new SimpleApplicationService();

    @Test
    void testHTTPSConnection() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IOException
    {
        // NOTE: To run this test we need to create a truststore has described here https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/
        //Arrange -> Nothing to do
        Unirest.shutDown();

        //System.out.println(HTTPSTest.class.getResource("trustStore.jks").getFile());
       // System.out.println(HTTPSTest.class.getResource("/").getFile());

        SSLContext sslContext =  new SSLContextBuilder().loadTrustMaterial(
                HTTPSTest.class.getResource("/trustStore.jks"), //path to jks file
                "changeit".toCharArray(), //enters in the truststore password for use
                new TrustSelfSignedStrategy() //will trust own CA and all self-signed certs
        ).build();


       CloseableHttpClient customHttpClient = HttpClients.custom().setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setSSLContext(sslContext).build();

        Unirest.config().httpClient(customHttpClient);
        Unirest.config().asyncClient(client); 

        /*Unirest.config().verifySsl(false);
        Unirest.config().hostnameVerifier(new NoopHostnameVerifier());
          */


        var properties = new Properties();
        var defaultHost = "0.0.0.0";
        var defaultPort = 7001;
        var defaultHTTPSPort = 8080;

        properties.put(RESTfulRPCAdapter.HOST_PROPERTY, defaultHost);
        properties.put(RESTfulRPCAdapter.PORT_PROPERTY, Integer.toString(defaultPort));
        properties.put(RESTfulRPCAdapter.HTTPS_PORT_PROPERTY, Integer.toString(defaultHTTPSPort));
        properties.put(RESTfulRPCAdapter.KEYSTORE_PASSWORD, "test123");
        properties.put(RESTfulRPCAdapter.KEYSTORE, "keystore.jks");

        //System.setProperty("javax.net.ssl.trustStore", "trustStore.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        var objectUnderTest = new RESTfulRPCAdapter(properties);
        objectUnderTest.register(simpleApplicationService);
        objectUnderTest.start();


        //Act
        Integer result = Unirest.get(REST_PATH_HTTPS + METHOD_GET_SIMPLE_VALUE)
                .header(CONTENT_TYPE, APPLICATION_TYPE)
                .asObject(Integer.class).getBody();


        //Assert
        assertNotNull(result);
        assertEquals(DEFAULT_VALUE, simpleApplicationService.getSimpleValue());
        assertEquals(simpleApplicationService.getSimpleValue(), result.intValue() );
    }

    @AfterEach
    void tearDown()
    {
        Unirest.shutDown();
    }
}
