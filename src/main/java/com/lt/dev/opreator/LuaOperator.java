package com.lt.dev.opreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuaOperator <T extends Serializable>  {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;


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

    }


}
