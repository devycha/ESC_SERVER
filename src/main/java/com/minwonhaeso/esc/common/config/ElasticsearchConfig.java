package com.minwonhaeso.esc.common.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.minwonhaeso.esc.stadium.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    @Value("${elasticsearch.url}")
    private String hostUrl;

    @Value("${elasticsearch.clusterName}")
    private String clusterName;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostUrl)
                .build();
        return RestClients.create(clientConfiguration).rest();
    }

//    @Bean
//    public ElasticsearchOperations elasticsearchTemplate() {
//        return new ElasticsearchRestTemplate(elasticsearchClient());
//    }
}


