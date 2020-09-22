package com.example.surl.repository;

import com.example.surl.config.CacheConfig;
import com.example.surl.model.ShortUrl;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlRepository extends MongoRepository<ShortUrl, String> {
    @Cacheable(value = CacheConfig.CACHE_SURLY)
    ShortUrl findByKeyCode(Long key);

    ShortUrl findByLongUrlAndUserId(String url, String userId);

    List<ShortUrl> findByUserId(String userId);

    @CachePut(value = CacheConfig.CACHE_SURLY, key = "#shortUrl.keyCode")
    ShortUrl save(ShortUrl shortUrl);
}
