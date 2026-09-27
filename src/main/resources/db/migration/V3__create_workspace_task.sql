CREATE TABLE workspace_task (
    id VARCHAR(255) PRIMARY KEY,
    workspace_id VARCHAR(255) NOT NULL,
    task TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_workspace_task_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES engineering_workspace(id)
        ON DELETE CASCADE
);