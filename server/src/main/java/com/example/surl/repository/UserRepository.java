package com.example.surl.repository;

import com.example.surl.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUserId(String userId);
    User save(User user);
}
