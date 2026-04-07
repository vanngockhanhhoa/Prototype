package org.example.ash.entity.oracle;

import jakarta.persistence.*;
import lombok.Data;
import org.example.ash.entity.Auditable;

import java.util.List;

@Entity
@Table(name = "category")
@Data
public class Category extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @Column(name = "status_queue")
    private String statusQueue;

    @Column(name = "retry_queue")
    private Integer retryQueue;
}
