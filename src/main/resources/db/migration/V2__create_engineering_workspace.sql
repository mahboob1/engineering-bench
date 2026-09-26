CREATE TABLE engineering_workspace (
    id VARCHAR(255) PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    revision VARCHAR(255),

    CONSTRAINT fk_engineering_workspace_project
        FOREIGN KEY (project_id)
        REFERENCES engineering_project(id)
        ON DELETE CASCADE
);