CREATE TABLE engineering_project (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    source_repository_url TEXT NOT NULL,
    source_repository_revision VARCHAR(255),

    working_repository_url TEXT NOT NULL,
    working_repository_revision VARCHAR(255),

    collection VARCHAR(255),

    technology_language VARCHAR(255),
    technology_framework VARCHAR(255),
    technology_build_tool VARCHAR(255),

    capabilities JSONB
);