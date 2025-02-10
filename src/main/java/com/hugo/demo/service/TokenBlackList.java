package com.hugo.demo.service;

public interface TokenBlackList {

    boolean addToBlacklist(String token);

    boolean isBlacklisted(String token);
}
