DROP TABLE IF EXISTS product;
 
CREATE TABLE product (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  product_Id VARCHAR(25) NOT NULL,
  title VARCHAR(250) NOT NULL,
  description VARCHAR(250) NOT NULL,
  brand VARCHAR(25) NOT NULL,
  price INT NOT NULL,
  color VARCHAR(25) NOT NULL
);
 
INSERT INTO product (id, product_Id, title, description, brand, price, color) VALUES
  (10001,'GAS1234567', 'Jeans', 'Slim fit jeans', 'GAS', 10000, 'Blue'),
  (10002,'REP7876543', 'Jeans', 'Straight fit jeans', 'REPLAY', 15000, 'Light Blue'),
  (10003,'BOS9987676', 'Shirt', 'Button Down Oxford', 'BOSS', 12000, 'White');