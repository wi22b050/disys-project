CREATE TABLE IF NOT EXISTS public.station
(
    id
        SERIAL
        PRIMARY
            KEY,
    db_url
        VARCHAR
            (
            255
            ) NOT NULL,
    lat REAL NOT NULL,
    lng REAL NOT NULL
);


INSERT INTO public.station(id, db_url, lat, lng)
VALUES (1, 'localhost:7779', '48.184192', '16.378604'),
       (2, 'localhost:7780', '48.186116', '16.377746'),
       (3, 'localhost:7781', '48.232940', '16.376786');