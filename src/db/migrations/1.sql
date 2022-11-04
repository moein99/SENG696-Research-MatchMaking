CREATE TABLE user (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username varchar(255) NOT NULL UNIQUE,
    encrypted_password varchar(255) NOT NULL ,
    user_type varchar(255) NOT NULL DEFAULT "C",
    name varchar(255),
    website varchar(255),
    logo_address varchar(255),
    resume_address varchar(255),
    hourly_compensation int,
    is_verified bool,
    subscription_ends TIMESTAMP,
    balance int default 0
);

-- INSERT INTO user (username, encrypted_password, user_type, name, website, logo_address, resume_address, hourly_compensation, is_verified, subscription_ends, balance) values ("moein", "12345", "C", "test", "test.com", "logo.png", "resume.pdf", 40, true, "2022-10-25 09:21:01", 5);

CREATE TABLE keyword (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    text varchar (255) NOT NULL
);

-- INSERT INTO keyword (text) values ("scraping");

CREATE TABLE userKeyword (
    user_id int NOT NULL,
    keyword_id int NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (keyword_id) REFERENCES keyword(id),
    CONSTRAINT pk_user_keyword PRIMARY KEY (user_id, keyword_id)
);

-- INSERT INTO userKeyword (user_id, keyword_id) values (2, 1);

CREATE TABLE project (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    owner_id int NOT NULL,
    description varchar(255) NOT NULL,
    assignee_id int DEFAULT NULL,
    progress int default 0,
    deadline TIMESTAMP NOT NULL,
    status varchar(255) DEFAULT "C",
    FOREIGN KEY (owner_id) REFERENCES user(id),
    FOREIGN KEY (assignee_id) REFERENCES user(id)
);

-- INSERT INTO project (owner_id, description, deadline) values (1, "An awesome project!", "2022-10-25 09:21:01");

CREATE TABLE bid (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    bidder_id int,
    project_id int,
    hourly_rate int,
    status varchar(255) DEFAULT "SP",
    FOREIGN KEY (bidder_id) REFERENCES user(id),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- INSERT INTO bid (bidder_id, project_id, hourly_rate) values (3, 2, 40);

CREATE TABLE contract (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    provider_id int NOT NULL ,
    client_id int NOT NULL,
    description varchar(255) NOT NULL ,
    accepted_by_provider bool default NULL,
    accepted_by_client bool default NULL,
    FOREIGN KEY (provider_id) REFERENCES user(id),
    FOREIGN KEY (client_id) REFERENCES user(id)
);

-- INSERT INTO contract (provider_id, client_id, description) values (2, 3, "30% of the project budget will be for the platform");

CREATE TABLE transaction (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    sender_id int NOT NULL,
    receiver_id int NOT NULL,
    description varchar(255),
    amount int NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id),
    FOREIGN KEY (receiver_id) REFERENCES user(id)
);

-- INSERT INTO transaction (sender_id, receiver_id, description, amount) values (2, 3, "This amount is for the project with title an Awesome project", 450);

CREATE TABLE message (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    sender_id int NOT NULL,
    receiver_id int NOT NULL,
    project_id int NOT NULL,
    text varchar(255) NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id),
    FOREIGN KEY (receiver_id) REFERENCES user(id),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- INSERT INTO message (sender_id, receiver_id, project_id, text) values (2, 3, 2, "Hello, do you need anything?");

CREATE TABLE feedback (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    sender_id int NOT NULL,
    receiver_id int NOT NULL,
    project_id int NOT NULL,
    comment varchar(255),
    rate int NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id),
    FOREIGN KEY (receiver_id) REFERENCES user(id),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- INSERT INTO feedback (sender_id, receiver_id, project_id, comment, rate) values (2, 3, 2, "That was a great work!", 5);