-- Create diff_reports table
CREATE TABLE diff_reports (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    base_api_specification_id UUID,
    candidate_api_specification_id UUID NOT NULL,
    base_spec_version VARCHAR(50),
    candidate_spec_version VARCHAR(50) NOT NULL,
    has_breaking_changes BOOLEAN NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_report_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_base_spec FOREIGN KEY (base_api_specification_id) REFERENCES api_specifications(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_candidate_spec FOREIGN KEY (candidate_api_specification_id) REFERENCES api_specifications(id) ON DELETE CASCADE
);

-- Create violations table
CREATE TABLE violations (
    id UUID PRIMARY KEY,
    diff_report_id UUID NOT NULL,
    rule_type VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    path VARCHAR(255) NOT NULL,
    http_method VARCHAR(20),
    message TEXT NOT NULL,
    CONSTRAINT fk_violation_report FOREIGN KEY (diff_report_id) REFERENCES diff_reports(id) ON DELETE CASCADE
);
