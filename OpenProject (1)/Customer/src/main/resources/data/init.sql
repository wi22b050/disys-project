-- Dumping data into database on start of project

CREATE TABLE IF NOT EXISTS public.customer(
    id serial PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

INSERT INTO public.customer(id, first_name, last_name)
VALUES (1, 'Luisa', 'Colon'),
       (2, 'Ismail', 'Southern'),
       (4, 'Test', 'Southern'),
       (3, 'Kory', 'Morley');


