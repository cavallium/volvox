INSERT INTO chat(id, status, name, username) VALUES (9007199256673076, 1, 'My Supergroup', 'mysupergroup');
INSERT INTO chat_name(chat_id, id, time, name) VALUES (9007199256673076, 12345000, current_timestamp, 'My Supergroup');
INSERT INTO chat_username(chat_id, id, time, username) VALUES (9007199256673076, 12345001, current_timestamp, 'mysupergroup');

INSERT INTO chat(id, status, name, username) VALUES (777000, 1, 'Telegram', 'telegram');
INSERT INTO chat_name(chat_id, id, time, name) VALUES (777000, 12345002, current_timestamp, 'Telegram');
INSERT INTO chat_username(chat_id, id, time, username) VALUES (777000, 12345003, current_timestamp, 'telegram');


INSERT INTO chat(id, status, name) VALUES (4503599627464345, 1, 'School group');
INSERT INTO chat_name(chat_id, id, time, name) VALUES (4503599627464345, 12345004, current_timestamp, 'School group');

INSERT INTO chat(id, status, name) VALUES (4503599627382355, 0, 'Old school group');
INSERT INTO chat_name(chat_id, id, time, name) VALUES (4503599627382355, 12345005, current_timestamp, 'Old school group');
