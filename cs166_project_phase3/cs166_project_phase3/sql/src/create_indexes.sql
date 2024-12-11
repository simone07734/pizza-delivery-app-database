CREATE INDEX user_login ON Users(login);
CREATE INDEX item_name ON Items(itemName);
CREATE INDEX store_id ON Store(storeID);
CREATE INDEX order_ID ON FoodOrder(orderID);
CREATE INDEX items_in_order_id ON ItemsInOrder(orderID);
