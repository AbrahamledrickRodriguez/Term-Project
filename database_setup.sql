create database if not exists meal_planner;

use meal_planner;

create table if not exists user (
    u_id int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    primary key (u_id),
    unique (username)
);

create table if not exists food (
    f_id int auto_increment,
    f_name varchar(255) not null,
    calories int not null,
    primary key (f_id)
);


create table if not exists dininghall (
    dh_id int auto_increment,
    dh_name varchar(255) not null,
    primary key (dh_id)
);

CREATE TABLE IF NOT EXISTS MealPlan (
    m_id INT AUTO_INCREMENT,
    m_name VARCHAR(255) NOT NULL,
    u_id INT NOT NULL,
    dh_id INT NOT NULL,
    PRIMARY KEY (m_id),
    FOREIGN KEY (u_id) REFERENCES User(u_id),
    FOREIGN KEY (dh_id) REFERENCES DiningHall(dh_id)
);

CREATE TABLE IF NOT EXISTS FoodInMealPlan (
    m_id INT NOT NULL,
    f_id INT NOT NULL,
    PRIMARY KEY (m_id, f_id),
    FOREIGN KEY (m_id) REFERENCES MealPlan(m_id),
    FOREIGN KEY (f_id) REFERENCES Food(f_id)
);

CREATE TABLE IF NOT EXISTS FoodInDiningHall (
    dh_id INT NOT NULL,
    f_id INT NOT NULL,
    PRIMARY KEY (dh_id, f_id),
    FOREIGN KEY (dh_id) REFERENCES DiningHall(dh_id),
    FOREIGN KEY (f_id) REFERENCES Food(f_id)
);

insert into dininghall(dh_name) values ("Bolton");

insert into dininghall(dh_name) values ("Snelling");

insert into dininghall(dh_name) values ("O-House");

insert into dininghall(dh_name) values ("Joe Frank");

insert into dininghall(dh_name) values ("The Niche");

-- See readme for instructions on how to import the food and FoodInDiningHall table values.

