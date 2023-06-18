package com.getyourtravel.entities.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findById(long id);
    Product findByNameAndCategoryIdAndIdNot(String productName, long categoryId, long id);
    Page<Product> findByCategoryId(long categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM Product p" +
            " WHERE p.category_id = :categoryId" +
            " ORDER BY p.sorting ASC", nativeQuery = true)
    Page<Product> findByCategoryIdAndSorting(@Param("categoryId") long categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM Product p" +
            " ORDER BY p.sorting ASC", nativeQuery = true)
    Page<Product> findAllBySorting(Pageable pageable);

    @Query(value = "SELECT * FROM Product p" +
            " WHERE p.category_id = :categoryId", nativeQuery = true)
    List<Product> findByCategoryId(@Param("categoryId") long categoryId);

    @Query(value = "SELECT * FROM Product p" +
            " WHERE p.category_id = :categoryId" +
            " ORDER BY p.sorting ASC LIMIT :quantity", nativeQuery = true)
    List<Product> findByCategoryIdAndSorting(@Param("categoryId") long categoryId, @Param("quantity") int quantity);

    @Query(value = "SELECT * FROM Product p" +
            " ORDER BY p.sorting ASC LIMIT :quantity", nativeQuery = true)
    List<Product> findAllBySorting(@Param("quantity") int quantity);

    @Query(value = "SELECT p.sorting FROM Product p" +
            " ORDER BY p.sorting DESC LIMIT 1", nativeQuery = true)
    Integer getLastSortingValue();
}