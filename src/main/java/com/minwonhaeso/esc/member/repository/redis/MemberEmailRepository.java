package com.minwonhaeso.esc.member.repository.redis;

import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import org.springframework.data.repository.CrudRepository;


public interface MemberEmailRepository extends CrudRepository<MemberEmail, String> {
}
