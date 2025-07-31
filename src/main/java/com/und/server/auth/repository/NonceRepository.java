package com.und.server.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.und.server.auth.entity.Nonce;

@Repository
public interface NonceRepository extends CrudRepository<Nonce, String> { }
