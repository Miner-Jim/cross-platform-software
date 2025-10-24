package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Token;
import com.example.demo.model.User;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long>{
    List<Token> findByUser(User user);
    List<Token> findByUserAndDisabledFalse(User user);
    Optional<Token> findByValueAndDisabledFalse(String value);
}
