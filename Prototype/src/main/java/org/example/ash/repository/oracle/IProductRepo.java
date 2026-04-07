package org.example.ash.repository.oracle;

import org.example.ash.entity.oracle.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IProductRepo extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findById(Long id);
}
