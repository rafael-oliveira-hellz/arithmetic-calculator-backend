INSERT INTO operation (id, type, cost) VALUES
(gen_random_uuid(), 'ADDITION', 5),
(gen_random_uuid(), 'SUBTRACTION', 5),
(gen_random_uuid(), 'MULTIPLICATION', 5),
(gen_random_uuid(), 'DIVISION', 5),
(gen_random_uuid(), 'SQUARE_ROOT', 5),
(gen_random_uuid(), 'RANDOM_STRING', 5)
ON CONFLICT DO NOTHING;
