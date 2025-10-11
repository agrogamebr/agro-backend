package br.com.agrogame.agrogame.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_company_name")
    private String fullCompanyName;

    @Column(name = "fantansy_name", nullable = false)
    private String fantansyName;

    @Column(name = "email1", nullable = false)
    private String email1;

    @Column(name = "email2")
    private String email2;

    @Column(name = "phone1", nullable = false)
    private String phone1;

    @Column(name = "phone2")
    private String phone2;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "responsible_name")
    private String responsibleName;

    @Column(name = "responsible_phone")
    private String responsiblePhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_status_id", nullable = false)
    private CompanyStatus companyStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_type_id", nullable = false)
    private CompanyType companyType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
