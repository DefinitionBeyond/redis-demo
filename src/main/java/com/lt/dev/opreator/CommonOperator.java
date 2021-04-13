package com.lt.dev.opreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class CommonOperator<T extends Serializable> {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    public void setString(String key, T val){
        try{
            boolean res = redisTemplate.opsForValue().setIfAbsent(key, val);
            System.out.println(String.format("set key: %s  value : %s  reslut %s", key, val, res));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Object getString(String key){
        try{
            return redisTemplate.opsForValue().get(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
