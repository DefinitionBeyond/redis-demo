package com.lt.dev.opreator;

import com.lt.dev.model.SimpleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;

@Service
public class SubPubOperator<T extends Serializable> {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    private ChannelTopic topic = new ChannelTopic("/redis/pubsub");

    public void publish(String publisher, T content) {
        System.out.println(String.format("message send {%s} by {%s}", content, publisher));

        SimpleMessage pushMsg = new SimpleMessage();
        pushMsg.setContent(content);
        pushMsg.setCreateTime(new Date());
        pushMsg.setPublisher(publisher);

        redisTemplate.convertAndSend(topic.getTopic(), pushMsg);
    }


}
