package com.higgin.book.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileManager {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ProfileManager.class);

    @Autowired
    private Environment environment;

    ProfileManager(Environment environment){
        this.environment =  environment;
        getActiveProfiles();

    }

    public void getActiveProfiles() {
        System.out.println("Checking profile details ");
        for (String profileName : environment.getActiveProfiles()) {
            LOGGER.info("Currently active profile - " + profileName.toString());
        }

        for (String properties : environment.getDefaultProfiles()) {
            LOGGER.info("Currently active properties - " + properties.toString());
        }
    }
}
