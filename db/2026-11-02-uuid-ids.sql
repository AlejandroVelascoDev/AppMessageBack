-- Migra users.id y chats.id de BIGINT a UUID preservando los datos.
-- Los UUID se derivan de forma determinista del id numerico original.
-- Las tablas hijas (chat_participants, messages) se actualizan en la misma transaccion.

BEGIN;

ALTER TABLE users ADD COLUMN new_id UUID;
UPDATE users SET new_id = md5('user:' || id)::uuid;
ALTER TABLE users ALTER COLUMN new_id SET NOT NULL;

ALTER TABLE chats ADD COLUMN new_id UUID;
UPDATE chats SET new_id = md5('chat:' || id)::uuid;
ALTER TABLE chats ALTER COLUMN new_id SET NOT NULL;

ALTER TABLE chat_participants ADD COLUMN new_chat_id UUID;
ALTER TABLE chat_participants ADD COLUMN new_user_id UUID;
UPDATE chat_participants cp
SET new_chat_id = c.new_id,
    new_user_id = u.new_id
FROM chats c, users u
WHERE cp.chat_id = c.id AND cp.user_id = u.id;
ALTER TABLE chat_participants ALTER COLUMN new_chat_id SET NOT NULL;
ALTER TABLE chat_participants ALTER COLUMN new_user_id SET NOT NULL;

ALTER TABLE messages ADD COLUMN new_chat_id UUID;
ALTER TABLE messages ADD COLUMN new_sender_id UUID;
UPDATE messages m
SET new_chat_id = c.new_id,
    new_sender_id = u.new_id
FROM chats c, users u
WHERE m.chat_id = c.id AND m.sender_id = u.id;
ALTER TABLE messages ALTER COLUMN new_chat_id SET NOT NULL;
ALTER TABLE messages ALTER COLUMN new_sender_id SET NOT NULL;

ALTER TABLE chat_participants DROP CONSTRAINT IF EXISTS chat_participants_chat_id_fkey;
ALTER TABLE chat_participants DROP CONSTRAINT IF EXISTS chat_participants_user_id_fkey;
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_chat_id_fkey;
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_sender_id_fkey;

ALTER TABLE chat_participants DROP CONSTRAINT IF EXISTS chat_participants_pkey;

ALTER TABLE chat_participants DROP COLUMN chat_id;
ALTER TABLE chat_participants DROP COLUMN user_id;
ALTER TABLE chat_participants RENAME COLUMN new_chat_id TO chat_id;
ALTER TABLE chat_participants RENAME COLUMN new_user_id TO user_id;
ALTER TABLE chat_participants ADD PRIMARY KEY (chat_id, user_id);

ALTER TABLE messages DROP COLUMN chat_id;
ALTER TABLE messages DROP COLUMN sender_id;
ALTER TABLE messages RENAME COLUMN new_chat_id TO chat_id;
ALTER TABLE messages RENAME COLUMN new_sender_id TO sender_id;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE chats DROP CONSTRAINT IF EXISTS chats_pkey;
ALTER TABLE chats DROP COLUMN id;
ALTER TABLE chats RENAME COLUMN new_id TO id;
ALTER TABLE chats ADD PRIMARY KEY (id);
ALTER TABLE chats ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE chat_participants
    ADD CONSTRAINT chat_participants_chat_id_fkey FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    ADD CONSTRAINT chat_participants_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE messages
    ADD CONSTRAINT messages_chat_id_fkey FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    ADD CONSTRAINT messages_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_chat_participants_user_id
    ON chat_participants (user_id);

CREATE INDEX IF NOT EXISTS idx_messages_chat_id
    ON messages (chat_id);

CREATE INDEX IF NOT EXISTS idx_messages_sender_id
    ON messages (sender_id);

CREATE INDEX IF NOT EXISTS idx_messages_chat_timestamp
    ON messages (chat_id, "timestamp");

COMMIT;
