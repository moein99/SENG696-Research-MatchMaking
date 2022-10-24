CREATE TABLE user (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username varchar(255) NOT NULL,
    encrypted_password varchar(255) NOT NULL ,
    user_type varchar(255) NOT NULL DEFAULT "C",
    name varchar(255),
    website varchar(255),
    logo_address varchar(255),
    resume_address varchar(255),
    hourly_compensation int,
    is_verified bool,
    subscription_ends TIMESTAMP
);

/*
INSERT INTO user (username, encrypted_password, user_type, name, website, logo_address, resume_address, hourly_compensation, is_verified, subscription_ends) values ("moein", "12345", "C", "test", "test.com", "logo.png", "resume.pdf", 40, true, "2022-10-25 09:21:01");
*/