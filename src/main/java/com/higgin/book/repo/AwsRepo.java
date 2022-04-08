package com.higgin.book.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class AwsRepo  {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AwsRepo.class);


}
