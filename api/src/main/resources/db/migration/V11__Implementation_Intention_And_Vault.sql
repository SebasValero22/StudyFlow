-- V11__Implementation_Intention_And_Vault.sql
-- Add Implementation Intention fields to Tasks + VaultItems table

-- Add Implementation Intention fields to Tasks
ALTER TABLE Tasks
    ADD COLUMN `trigger` VARCHAR(500) NULL,
    ADD COLUMN microStep VARCHAR(500) NULL,
    ADD COLUMN energyBucket VARCHAR(20) NULL;

-- VaultItems table for "Say No" Anti-FOMO Vault
CREATE TABLE VaultItems (
    vaultItemId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    domain VARCHAR(20) NULL,
    reason VARCHAR(500) NULL,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deletedAt TIMESTAMP NULL,
    clientUuid VARCHAR(36) NULL,
    CONSTRAINT fk_vault_items_user FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE
);