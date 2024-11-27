DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Items CASCADE;
DROP TABLE IF EXISTS FoodOrder CASCADE;
DROP TABLE IF EXISTS Store CASCADE;
DROP TABLE IF EXISTS ItemsInOrder CASCADE;

CREATE TABLE Users ( login varchar(50) NOT NULL,
                     password varchar(30) NOT NULL,
                     role char(20) NOT NULL,
                     favoriteItems text,
                     phoneNum varchar(20) NOT NULL,
                     PRIMARY KEY(login)
);

CREATE TABLE Items ( itemName varchar(50) NOT NULL,
                       ingredients varchar(300) NOT NULL,
                       typeOfItem varchar(30) NOT NULL,
                       price decimal(10,2) NOT NULL,
                       description text,
                       PRIMARY KEY(itemName)
);

CREATE TABLE Store ( storeID integer NOT NULL,
                           address varchar(50) NOT NULL,
                           city varchar(50) NOT NULL,
                           state varchar(60) NOT NULL,
                           isOpen varchar(60) NOT NULL,
                           reviewScore float,
                           PRIMARY KEY(storeID)
);

CREATE TABLE FoodOrder ( orderID integer NOT NULL,
                           login varchar(50) NOT NULL, --places relationship
                           storeID integer NOT NULL, --placedAt relationship
                           totalPrice decimal(10,2) NOT NULL,
                           orderTimestamp timestamp NOT NULL,
                           orderStatus char(50),
                           PRIMARY KEY(orderID),
                           FOREIGN KEY(login) REFERENCES Users(login)
                           ON DELETE CASCADE,
                           FOREIGN KEY(storeID) REFERENCES Store(storeID)
                           ON DELETE CASCADE
);



CREATE TABLE ItemsInOrder ( orderID integer NOT NULL,
                           itemName varchar(50) NOT NULL,
                           quantity integer NOT NULL,
                           PRIMARY KEY(orderID, itemName),
                           FOREIGN KEY(orderID) REFERENCES FoodOrder(orderID) ON DELETE CASCADE,
                           FOREIGN KEY(itemName) REFERENCES Items(itemName)
                           ON DELETE CASCADE
);

