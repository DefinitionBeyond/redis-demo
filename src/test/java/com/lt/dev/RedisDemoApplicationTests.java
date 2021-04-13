package com.lt.dev;

import com.lt.dev.opreator.CommonOperator;
import com.lt.dev.opreator.LuaOperator;
import com.lt.dev.opreator.SubPubOperator;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@RunWith(SpringRunner.class)
class RedisDemoApplicationTests {

    @Autowired
    CommonOperator command;

    @Autowired
    SubPubOperator subPubOperator;

    @Autowired
    LuaOperator luaOperator;

    @Autowired
    RedisClient redisClient;

    @Test
    void contextLoads() {
    }

    @Test
    public void testString() {
        String key = "lt-test";
        String val = "ok";
        command.setString(key, val);
        Assert.assertEquals(command.getString(key), val);
    }

    @Test
    public void subPubTest() {

        String topic = "sub-test";
        String message = "once-test";
        subPubOperator.publish("test", "hey you must go now!");

    }

    @Test
    public void threadTestSub() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                new PubThread().start("admin", "hahaha");
            } else {
                new PubThread().start("test", "hey you must go now!");
            }
            Thread.sleep(50);
        }

    }

    class PubThread extends Thread {

        public void start(String publisher, String message) {
            Thread t = new Thread(() -> {
                for (int i = 0; i < 5; i++)
                    subPubOperator.publish(publisher, message + currentThread().getName() + i);
            });
            t.setDaemon(false);
            t.start();
        }
    }

    @Test
    public void testLua() {
        luaOperator.eval();
    }

    @Test
    public void clientTest() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        StatefulRedisConnection<String, String> redisConnection = redisClient.connect();

        //同步
        RedisCommands<String, String> redisCommands = redisConnection.sync();
        //异步
//        RedisAsyncCommands<String, String> redisCommands = redisConnection.async();
        //reactive
//        RedisReactiveCommands<String, String> redisCommands = redisConnection.reactive();

        //发布
        threadPool.submit(() -> {

                    redisCommands.publish("__keyevent@0__:expired", "Ex");

                    while (true) {
                        redisCommands.publish("CCTV5", "NBA" + System.currentTimeMillis());
                    }
                }
        );


        Thread.sleep(10000);

        //订阅
        // 只接收键过期的事件
//        redisCommands.publish("__keyevent@0__:expired", "Ex");
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();

        connection.addListener(new RedisPubSubAdapter<String, String>() {

            //按模式匹配channel
            @Override
            public void psubscribed(String pattern, long count) {
                System.out.println(String.format("pattern:{%s},count:{%s}", pattern, count));
            }

            //按照指定channel
            @Override
            public void subscribed(String channel, long count) {
                super.subscribed(channel, count);
            }

            //取消订阅channel
            @Override
            public void unsubscribed(String channel, long count) {
                super.unsubscribed(channel, count);
            }

            @Override
            public void message(String pattern, String channel, String message) {
                System.out.println(String.format("pattern:{%s},channel:{%s},message:{%s}", pattern, channel, message));
            }
        });

        //同步
//        RedisPubSubCommands<String, String> commands = connection.sync();

        //异步
        RedisPubSubAsyncCommands<String, String> commands = connection.async();

//        RedisPubSubReactiveCommands<String, String> commands = connection.reactive();

        threadPool.submit(() -> {
                    redisCommands.setex("name", 2, "throwable");
//                    commands.psubscribe("__keyevent@0__:expired");

                    while (true) {
//                        commands.psubscribe("CCTV*");


                        RedisFuture<Void> future = commands.psubscribe("CCTV*");
                        System.out.println("Async sub message : "+future.get());
//                        future.get();
                        future.thenAccept(message->{
                            System.out.println("Async sub message : "+message);}
                            );
//                        future.thenApply(message->{
//                            System.out.println("Async sub message : "+message);
//                            return Mono.empty();
//                        });

//                        commands.psubscribe("CCTV*")
//                                .subscribe();
//                        commands.observeChannels()
//                                .doOnNext(message->{
//                            System.out.println(String.format( "Async sub channel {%s} message : {%s}",message.getChannel(), message.getMessage()));
//
////                             Flux.just(message);
//                        }).subscribe()
//                        ;
                    }
                }
        );
        Thread.sleep(1000);
//        redisConnection.close();
//        connection.close();
//        redisClient.shutdown();
    }
}
