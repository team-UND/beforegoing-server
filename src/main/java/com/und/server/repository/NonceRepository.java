package com.und.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.und.server.entity.Nonce;

@Repository
public interface NonceRepository extends CrudRepository<Nonce, String> { }
