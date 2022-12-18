package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.stadium.model.entity.StadiumPayment;
import org.springframework.data.repository.CrudRepository;


public interface StadiumPaymentRepository extends CrudRepository<StadiumPayment, String> {
}
