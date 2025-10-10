package com.library.sportshop.repository;

import com.library.sportshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByUserCodeAndProduct_Id(String userCode, Integer productId);
    List<CartItem> findByUserCode(String userCode);
    @Modifying
    @Transactional
    void deleteByUserCodeAndProduct_Id(String userCode, Integer productId);

    @Query("select coalesce(sum(c.quantity),0) from CartItem c where c.userCode = ?1")
    Integer countQuantityByUserCode(String userCode);
}


