package com.sping.cloud.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class QueryService {

    @Autowired
    InteractiveQueryService interactiveQueryService;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String topicName;

    @PostMapping("/publish")
    public String publishMessage(@RequestBody HashMap<String, String> request) {
        String message = request.get("message");
        System.out.println("Sending:"+message+" to "+ topicName);
        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topicName, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=["
                        + message + "] due to : " + ex.getMessage());
            }
        });
        return "Record Sent";
    }


    @GetMapping("/getValues")
    public HashMap<String, Long> getValues() {
        HashMap<String,Long> valueMap = new HashMap<>();
        KeyValueIterator<String, Long> storeIterator = interactiveQueryService.getQueryableStore("wordCountTable", QueryableStoreTypes.<String,Long>keyValueStore()).all();
        while (storeIterator.hasNext()) {
            KeyValue<String, Long> keyValue = storeIterator.next();
            valueMap.put(keyValue.key,keyValue.value);
        }
        return valueMap;
    }
}
