package org.example.ash.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name = "THROTTLE_TRANSACTION")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TRANSACTION_ID", length = 50, nullable = false)
    private String transactionId;

    @Column(name = "CREATED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "CREATED_BY", length = 20, nullable = false)
    private String createdBy;

    @Column(name = "LOCATION_CODE", length = 20)
    private String locationCode;

    @Column(name = "TRANSACTION_STATUS", nullable = false)
    private Integer transactionStatus;

    @Column(name = "UPDATED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "UPDATED_BY", length = 20)
    private String updatedBy;
}
