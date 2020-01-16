package com.run.secondkill.util;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public final class RedisUtil implements InitializingBean {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    //默认一天
    private static final long DEFAULT_TIMEOUT_SECOND = 60*60*24;

    private ValueOperations valueOperations;
    private HashOperations hashOperations;
    private SetOperations setOperations;
    private ZSetOperations zSetOperations;
    private HyperLogLogOperations hyperLogLogOperations;

    @Override
    public void afterPropertiesSet() throws Exception {
        valueOperations = redisTemplate.opsForValue();
        hashOperations = redisTemplate.opsForHash();
        setOperations = redisTemplate.opsForSet();
        zSetOperations = redisTemplate.opsForZSet();
        hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
    }


    public boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    public boolean hasKeys(Collection keys){
        return keys.size() == redisTemplate.countExistingKeys(keys);
    }

    public void set(String key,Object value,long seconds){
        valueOperations.set(key,value, Optional.ofNullable(seconds).orElse(DEFAULT_TIMEOUT_SECOND),TimeUnit.SECONDS);
    }

    public boolean setNX(String key,Object value,long second){
        return valueOperations.setIfAbsent(key,value,second, TimeUnit.SECONDS);
    }

    public Optional<Object> get(String key){
        return Optional.ofNullable(valueOperations.get(key));
    }

    public long getExpire(String key,TimeUnit timeUnit){
        return redisTemplate.getExpire(key,timeUnit);
    }

    public boolean remove(String key){
        return redisTemplate.delete(key);
    }

   public boolean casRemove(String key,String value){
       DefaultRedisScript<Long> script = new DefaultRedisScript<>();
       script.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/cas_delete_key.lua")));
       script.setResultType(Long.class);
       return redisTemplate.execute(script, Lists.newArrayList(key),value).equals(Long.valueOf(1));
   }

   public boolean expire(String key,long millTime){
        return redisTemplate.expire(key,millTime,TimeUnit.MILLISECONDS);
   }

   public boolean casExpireLock(String key,String value,long second){
       DefaultRedisScript<Long> script = new DefaultRedisScript<>();
       script.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/cas_expire_lock.lua")));
       script.setResultType(Long.class);
       return redisTemplate.execute(script, Lists.newArrayList(key),value,second).equals(Long.valueOf(1));
   }

}
