package org.example.ash.repository.oracle;

import jakarta.transaction.Transactional;
import org.example.ash.entity.oracle.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ICategoryRepo extends JpaRepository<Category, Long> {
    @EntityGraph(attributePaths = {"products"})
    List<Category> findAll();

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.statusQueue = :status WHERE c.id = :id")
    void updateStatus(Long id, String status);

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.retryQueue = COALESCE(c.retryQueue, 0) + 1, c.statusQueue = 'RETRY' WHERE c.id = :id")
    void increaseRetry(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.statusQueue = :status, c.retryQueue = 0 WHERE c.id = :id")
    void updateStatusDone(Long id, String status);


    @Transactional
    @Modifying
    @Query("""
        UPDATE Category c 
        SET 
            c.retryQueue = COALESCE(c.retryQueue, 0) + 1,
            c.statusQueue = CASE 
                WHEN COALESCE(c.retryQueue, 0) + 1 >= 3 THEN 'FAILED'
                ELSE 'RETRY'
            END
        WHERE c.id = :id
    """)
    void updateStatusFailed(Long id);
}
