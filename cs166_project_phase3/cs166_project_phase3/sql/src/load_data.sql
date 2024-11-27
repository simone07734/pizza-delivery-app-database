/* Replace the location to where you saved the data files*/
COPY Users
FROM 'REPLACE PATH HERE/users.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Items
FROM 'REPLACE PATH HERE/items.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Store
FROM 'REPLACE PATH HERE/store.csv'
WITH DELIMITER ',' CSV HEADER;

COPY FoodOrder
FROM 'REPLACE PATH HERE/foodorder.csv'
WITH DELIMITER ',' CSV HEADER;

COPY ItemsInOrder
FROM 'REPLACE PATH HERE/itemsinorder.csv'
WITH DELIMITER ',' CSV HEADER;
