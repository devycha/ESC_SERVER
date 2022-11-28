package com.minwonhaeso.esc.security.redis;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken,String> {
}
