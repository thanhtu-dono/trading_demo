MERGE INTO wallet (user_id, usdt_balance, btc_balance, eth_balance)
    KEY (user_id)
SELECT u.id,
       50000.0,
       0.0,
       0.0
FROM user u
WHERE u.username = 'tunguyen'
  AND NOT EXISTS (SELECT 1 FROM wallet w WHERE w.user_id = u.id);
