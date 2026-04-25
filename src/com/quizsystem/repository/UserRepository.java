package com.quizsystem.repository;

import com.quizsystem.model.User;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository — Interface for user persistence.
 */
public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    User save(User user);
    boolean deleteById(int id);
}
