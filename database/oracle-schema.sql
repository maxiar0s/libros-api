CREATE SEQUENCE books_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE books (
    id NUMBER(19, 0) NOT NULL,
    title VARCHAR2(255 CHAR) NOT NULL,
    author VARCHAR2(255 CHAR) NOT NULL,
    publication_year NUMBER(10, 0) NOT NULL,
    genre VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT books_pk PRIMARY KEY (id)
);

INSERT INTO books (id, title, author, publication_year, genre)
VALUES (books_seq.NEXTVAL, 'The Hobbit', 'J. R. R. Tolkien', 1937, 'Fantasy');

INSERT INTO books (id, title, author, publication_year, genre)
VALUES (books_seq.NEXTVAL, 'Nineteen Eighty-Four', 'George Orwell', 1949, 'Dystopian');

COMMIT;
