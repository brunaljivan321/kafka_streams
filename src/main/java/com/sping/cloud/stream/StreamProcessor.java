package com.sping.cloud.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Function;

@Component
public class StreamProcessor {
    @Bean
    public Function<KStream<String,String>, KStream<String,Long>> process() {
        return input -> input
                .flatMapValues(value -> {
                    System.out.println("Received Message:"+value);
                    return Arrays.asList(value.toLowerCase().split("\\W+"));
                })
                .map((key, value) -> new KeyValue<>(value,value))
                .groupByKey()
                .count(Materialized.as("wordCountTable"))
                .toStream();
    }
}
