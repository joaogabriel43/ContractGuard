package br.com.contractguard.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "diff_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiffReportJpaEntity {

    @Id
    private UUID id;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "base_api_specification_id")
    private UUID baseApiSpecificationId;

    @Column(name = "candidate_api_specification_id", nullable = false)
    private UUID candidateApiSpecificationId;

    @Column(name = "base_spec_version")
    private String baseSpecVersion;

    @Column(name = "candidate_spec_version", nullable = false)
    private String candidateSpecVersion;

    @Column(name = "has_breaking_changes", nullable = false)
    private boolean hasBreakingChanges;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ViolationJpaEntity> violations = new ArrayList<>();

    public void addViolation(ViolationJpaEntity violation) {
        violations.add(violation);
        violation.setReport(this);
    }
}
