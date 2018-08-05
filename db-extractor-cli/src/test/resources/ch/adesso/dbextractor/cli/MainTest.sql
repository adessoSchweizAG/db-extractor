CREATE TABLE Customer(
	ID INTEGER PRIMARY KEY,
	FirstName VARCHAR(20),
	LastName VARCHAR(30),
	Street VARCHAR(50),
	City VARCHAR(25)
);

CREATE TABLE Invoice(
	ID INTEGER PRIMARY KEY,
	CustomerID INTEGER,
	Total DECIMAL,
	FOREIGN KEY (CustomerId) REFERENCES Customer(ID) ON DELETE CASCADE
);

CREATE TABLE Product(
	ID INTEGER PRIMARY KEY,
	Name VARCHAR(30),
	Price DECIMAL
);

CREATE TABLE Item(
	ID INTEGER PRIMARY KEY,
	InvoiceID INTEGER,
	Item INTEGER,
	ProductID INTEGER,
	Quantity INTEGER,
	Cost DECIMAL,
	UNIQUE (InvoiceID, Item),
	FOREIGN KEY (InvoiceId) REFERENCES Invoice (ID) ON DELETE CASCADE,
	FOREIGN KEY (ProductId) REFERENCES Product(ID) ON DELETE CASCADE
);

INSERT INTO Customer VALUES(0,'Laura','Steel','429 Seventh Av.','Dallas');

INSERT INTO Invoice VALUES(0,0,0.0);

INSERT INTO Product VALUES(0,'Iron Iron',54);
INSERT INTO Product VALUES(1,'Chair Shoe',248);
INSERT INTO Product VALUES(2,'Telephone Clock',248);
INSERT INTO Product VALUES(3,'Chair Chair',254);
INSERT INTO Product VALUES(7,'Telephone Shoe',84);
INSERT INTO Product VALUES(14,'Telephone Iron',124);
INSERT INTO Product VALUES(47,'Ice Tea Iron',178);

INSERT INTO Item VALUES(0,0,2,47,3,1.5);
INSERT INTO Item VALUES(1,0,1,14,19,1.5);
INSERT INTO Item VALUES(2,0,0,7,12,1.5);

UPDATE Item SET Cost = Cost * (SELECT Price FROM Product prod WHERE ProductID=prod.ID);
UPDATE Invoice SET Total = (SELECT SUM(Cost*Quantity) FROM Item WHERE InvoiceID=Invoice.ID);