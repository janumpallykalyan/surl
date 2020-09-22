package com.example.surl.repository;

import com.example.surl.model.ShortUrlLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlLogRepository extends MongoRepository<ShortUrlLog, String> {

    List<ShortUrlLog> findShortUrlLogsByKeyCode(Long key);

    ShortUrlLog save(ShortUrlLog shortUrlLog);
}
