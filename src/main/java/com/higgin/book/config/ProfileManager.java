package com.higgin.book.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileManager {

    @Autowired
    private Environment environment;

    ProfileManager(Environment environment){
        this.environment =  environment;
        getActiveProfiles();

    }

    public void getActiveProfiles() {
        System.out.println("Checking profile details ");
        for (String profileName : environment.getActiveProfiles()) {
            System.out.println("Currently active profile - " + profileName);
        }

        for (String properties : environment.getDefaultProfiles()) {
            System.out.println("Currently active properties - " + properties);
        }
    }
}
