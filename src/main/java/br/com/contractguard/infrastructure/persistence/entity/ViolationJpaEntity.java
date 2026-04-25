package br.com.contractguard.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "violations")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ViolationJpaEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "diff_report_id", nullable = false)
    private DiffReportJpaEntity report;

    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String path;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(nullable = false)
    private String message;
}
