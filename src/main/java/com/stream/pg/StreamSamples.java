package com.stream.pg;

import com.higgin.book.repo.AwsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StreamSamples {

    protected static final Logger LOGGER = LoggerFactory.getLogger(StreamSamples.class);

    private static long counter;

    private void wasCalled() {
        counter++;
    }

    public static void callStreamLazy(){
        //Intermediate operations are lazy. This means that they will be invoked only if it is necessary for the terminal operation execution.
        List<String> list = Arrays.asList("abc1","abc2","abc3");
        counter = 0;
        Optional<String> stream = list.stream().filter(element -> {
            LOGGER.info("filter() was called : "+element);
            return element.contains("2");
        }).map(element -> {
            LOGGER.info("map() was called : "+element);
            return element.toUpperCase();
        }).findFirst();
    }


    public static void callStream(){
        List<String> list = Arrays.asList("abc1","abc2","abc3");
        counter = 0;
         List<String > finalList = list.stream().filter(element -> {
            LOGGER.info("filter() was called : "+element);
            return element.contains("2");
        }).map(element -> {
            LOGGER.info("map() was called : "+element);
            return element.toUpperCase();
        }).collect(Collectors.toList());

        System.out.println("finalList {} "+ finalList);
    }


    public static void main(String[] args) {
        callStream();
    }

}
