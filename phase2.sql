DROP TABLE IF EXISTS AppUser CASCADE;
DROP TABLE IF EXISTS Item CASCADE;
DROP TABLE IF EXISTS OrderPlaced CASCADE;
DROP TABLE IF EXISTS Store CASCADE;
DROP TABLE IF EXISTS views CASCADE;
DROP TABLE IF EXISTS has CASCADE;
DROP TABLE IF EXISTS availableAt CASCADE;

CREATE TABLE AppUser (
    login CHAR(15) NOT NULL,
    password CHAR(50) NOT NULL,
    phoneNumber CHAR(60) NOT NULL,
    role CHAR(10) NOT NULL,
    favoritItem CHAR(500),
    PRIMARY KEY (login)
);

CREATE TABLE Item (
    itemName CHAR(50) NOT NULL,
    type CHAR(40) NOT NULL,
    price FLOAT NOT NULL,
    ingredients CHAR(500) NOT NULL,
    description CHAR(600),
    imageURL CHAR(256),
    PRIMARY KEY (itemName)
);

CREATE TABLE Store (
    storeID CHAR(50) NOT NULL,
    address CHAR(40) NOT NULL,
    city CHAR(40) NOT NULL,
    state CHAR(40) NOT NULL,
    isOpen CHAR(40) NOT NULL,
    reviewScore FLOAT ,
    PRIMARY KEY (storeID)
);

CREATE TABLE views (
    login CHAR(15) NOT NULL,
    itemName CHAR(50) NOT NULL,
    PRIMARY KEY (login, itemName),
    FOREIGN KEY (login) REFERENCES AppUser
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (itemName) REFERENCES Item 
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE has(
    orderID CHAR(60) NOT NULL,
    itemName CHAR(50) NOT NULL,
    PRIMARY KEY (orderID, itemName),
    FOREIGN KEY (itemName) REFERENCES Item
);

CREATE TABLE OrderPlaced (
    orderID CHAR(60) NOT NULL,
    orderTimestamp TIMESTAMP NOT NULL,
    orderStatus CHAR(50) NOT NULL,
    totalPrice FLOAT NOT NULL,
    login CHAR(15) NOT NULL,
    storeID CHAR(50) NOT NULL,
    itemName CHAR(50) NOT NULL,
    --participation constraint
    CONSTRAINT hasForeignKey 
        FOREIGN KEY (orderID,itemName)
        REFERENCES has(orderID, itemName) DEFERRABLE INITIALLY DEFERRED,

    PRIMARY KEY (orderID),
    FOREIGN KEY (login) REFERENCES AppUser
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (storeID) REFERENCES Store
);

ALTER TABLE has
    ADD FOREIGN KEY (orderID) REFERENCES OrderPlaced
        ON DELETE CASCADE
        ON UPDATE CASCADE;


CREATE TABLE availableAt (
    itemName CHAR(50) NOT NULL,
    storeID CHAR(50) NOT NULL,
    PRIMARY KEY (itemName, storeID),
    FOREIGN KEY (itemName) REFERENCES Item
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (storeID) REFERENCES Store
        ON DELETE CASCADE
        ON UPDATE CASCADE
);