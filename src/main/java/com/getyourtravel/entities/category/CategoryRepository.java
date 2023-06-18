package com.getyourtravel.entities.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findById(long id);
    Category findByName(String name);
    Category findByNameAndIdNot(String name, long id);
    void deleteById(long id);

    @Query(value = "SELECT * FROM Category c " +
            "ORDER BY c.sorting ASC", nativeQuery = true)
    List<Category> findAllBySorting();

    @Query(value = "SELECT c.sorting FROM Category c " +
            "ORDER BY c.sorting DESC LIMIT 1", nativeQuery = true)
    Integer getLastSortingValue();
}