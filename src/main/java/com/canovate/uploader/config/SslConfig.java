package com.canovate.uploader.config;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@Slf4j
public class SslConfig {

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .retryer(Retryer.NEVER_RETRY)
                .decoder(new ResponseEntityDecoder(new feign.codec.Decoder.Default()))
                .encoder(new GsonEncoder())
                .contract(new SpringMvcContract())
                .client(new Client.Default(getSSLSocket().getSocketFactory(), new NoopHostnameVerifier()));
    }

    @Bean
    public RestTemplate restTemplate() {
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(getSSLSocket(), (s, sslSession) -> true);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    SSLContext getSSLSocket() {
        SSLContext sslContext = null;

        try {
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            };
            sslContext = SSLContextBuilder
                    .create()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (Exception e) {
            log.error("getSSLSocketFactory in SslConfig error", e);
        }
        return sslContext;
    }
}
