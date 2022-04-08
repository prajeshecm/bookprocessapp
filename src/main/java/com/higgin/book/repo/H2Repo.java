package com.higgin.book.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class H2Repo {

    protected static final Logger LOGGER = LoggerFactory.getLogger(H2Repo.class);


}
