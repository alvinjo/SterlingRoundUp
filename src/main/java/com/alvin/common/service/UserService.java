package com.alvin.common.service;

import com.alvin.common.repo.UserRepo;
import com.alvin.common.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepo repo;

    @Autowired
    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public Optional<User> getUserById(String id) {
        return repo.findById(id);
    }

}
