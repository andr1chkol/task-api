ALTER TABLE tasks ADD COLUMN owner_id BIGINT NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(id);

CREATE INDEX idx_tasks_owner_id ON tasks(owner_id);
