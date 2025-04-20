INSERT INTO user (username, full_name, email, phone, address)
SELECT 'user01',
       'User One',
       'user01@example.com',
       '0123456789',
       'Hanoi' WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE username = 'user01'
);