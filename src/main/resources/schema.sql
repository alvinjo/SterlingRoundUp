CREATE TABLE ROUND_UP_JOB (
    ID VARCHAR(255) PRIMARY KEY,
    ACCOUNT_ID VARCHAR(255),
    SAVINGS_GOAL_ID VARCHAR(255),
    TRANSFER_ID VARCHAR(255),
    STATUS VARCHAR(255)
);

CREATE TABLE USER_TABLE (
    ID VARCHAR(255) PRIMARY KEY,
    ENCRYPTED_ACCESS_TOKEN TEXT(1500)
);