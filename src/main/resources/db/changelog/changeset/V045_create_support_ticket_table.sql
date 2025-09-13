CREATE TABLE IF NOT EXISTS support_schema.support_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    issue_type VARCHAR(50) NOT NULL,
    selected_issue_id UUID REFERENCES support_schema.support_issue_presets(id) ON DELETE SET NULL,
    comment VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);