package com.hugo.demo.service.impl;

import java.util.HashSet;
import java.util.Set;

import com.hugo.demo.service.TokenBlackList;
import org.springframework.stereotype.Service;

@Service
public class InMemoryTokenBlackList implements TokenBlackList {
    private final Set<String> blacklist = new HashSet<>();

    @Override
    public boolean addToBlacklist(String token) {
        try {
            blacklist.add(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
