-- AI Coach conversation history table
CREATE TABLE IF NOT EXISTS ai_coach_messages (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE ai_coach_messages ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own messages"
    ON ai_coach_messages FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own messages"
    ON ai_coach_messages FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE INDEX idx_ai_coach_messages_user_id
    ON ai_coach_messages(user_id, created_at DESC);
