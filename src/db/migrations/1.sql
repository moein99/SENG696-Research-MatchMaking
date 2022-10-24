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
    is_verified bool
);
