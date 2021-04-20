package com.lt.dev.opreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.ws.Response;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LuaOperator <T extends Serializable>  {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, Long> reactiveRedisTemplate;

    public void eval(){

        List<String> keys = new ArrayList<String>();
        List<String> vals = new ArrayList<String>();
        StringBuffer lua=new StringBuffer();
        lua.append("local maxid=redis.call('get','max:'..KEYS[1]) ");
//        sb.append("local nullflag=string.split(maxid,2,2) ");
//        lua.append("local item ");
//        lua.append("if maxid%2==0 then ");
//        lua.append("return item ");
//        lua.append("else ");
        lua.append("local item=redis.call('keys','lt') ");
//        lua.append("end ");
        lua.append("return item ");
        RedisScript<List> script = RedisScript.of(lua.toString());
        keys.add("1");
        vals.add("1");
        List execute = redisTemplate.execute(script, keys, vals);
//        execute.forEach(
////            System.out::println
////        );
        System.out.println();

        DefaultRedisScript redisScript = new DefaultRedisScript(lua.toString(), String.class);


    }


    public Mono<Object> reactiveEval(){

        DefaultRedisScript<List<Long>> redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/request_rate_limit.lua")));

        List<String> keys = getKeys("appName");
        List<String> scriptArgs = Arrays.asList(2 + "", 10 + "", Instant.now().getEpochSecond() + "", "1");
        Flux<List<Long>> flux = reactiveRedisTemplate.execute(redisScript, keys, scriptArgs);
        return flux.onErrorResume((throwable) -> {
            return Flux.just(Arrays.asList(1L, -1L));
        }).reduce(new ArrayList(), (longs, l) -> {
            longs.addAll(l);
            return longs;
        }).map((results) -> {
            boolean allowed = (Long)results.get(0) == 1L;
            Long tokensLeft = (Long)results.get(1);
//            Response response = new Response(allowed, this.getHeaders(routeConfig, tokensLeft));
            return Mono.empty();
        });


    }
    static List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

}
