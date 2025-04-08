package com.github.khalaimovda.shopview.showcase.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
        CacheProperties cacheProperties
    ) {
        return builder -> {
            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(customObjectMapper());
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getRedis().getTimeToLive())
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
            builder.cacheDefaults(config);
        };
    }

    /**
     * Custom object mapper to skip default type processing for BigDecimal
     */
    public ObjectMapper customObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule bigDecimalModule = new SimpleModule();
        bigDecimalModule.addSerializer(BigDecimal.class, new ToStringSerializer());
        bigDecimalModule.addDeserializer(BigDecimal.class, new JsonDeserializer<BigDecimal>() {
            @Override
            public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new BigDecimal(p.getText());
            }
        });
        mapper.registerModule(bigDecimalModule);
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TypeResolverBuilder<?> typer = new StdTypeResolverBuilder() {

            @Override
            public TypeSerializer buildTypeSerializer(
                SerializationConfig config,
                JavaType baseType,
                Collection<NamedType> subtypes
            ) {
                if (baseType.isTypeOrSubTypeOf(BigDecimal.class)) {
                    return null;
                }
                return super.buildTypeSerializer(config, baseType, subtypes);
            }

            @Override
            public TypeDeserializer buildTypeDeserializer(
                DeserializationConfig config,
                JavaType baseType,
                Collection<NamedType> subtypes
            ) {
                if (baseType.isTypeOrSubTypeOf(BigDecimal.class)) {
                    return null;
                }
                return super.buildTypeDeserializer(config, baseType, subtypes);
            }
        };

        typer.init(JsonTypeInfo.Id.CLASS, null);
        typer.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        mapper.setDefaultTyping(typer);

        return mapper;
    }
}
