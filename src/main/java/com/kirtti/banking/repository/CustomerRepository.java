package com.kirtti.banking.repository;

import com.kirtti.banking.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CustomerRepository extends JpaRepository<Customers,String> {
    @Transactional
    @Modifying
    @Query("update Customers set cash=cash-:k where userId=:u")
    int  updateCus(double k, String u);

    @Transactional
    @Modifying
    @Query("update Customers set cash=cash+:k where userId=:u")
    int  updateTcus(Double k, String u);

    @Transactional
    @Modifying
    @Query("update Customers set password=:p where userId=:u")
    int updatePass(String u,String p);

}
