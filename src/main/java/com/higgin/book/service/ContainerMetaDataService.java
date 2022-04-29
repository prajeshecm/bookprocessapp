package com.higgin.book.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContainerMetaDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerMetaDataService.class);

    protected static final String container_url = "ECS_CONTAINER_METADATA_URI";
    private static final String DEFAULT_VALUE = "EMPTY";

    @Value("${"+container_url+":"+DEFAULT_VALUE+"}")
    private String containerMetaDataVar ;

    @Bean
    public String getContainerMetadataDetails(){
        LOGGER.info("Environment Variable : "+containerMetaDataVar);
        if (containerMetaDataVar.contains("EMPTY")) {
            return "NA";
        }
       return new RestTemplate().getForObject(container_url,String.class);
    }
}
