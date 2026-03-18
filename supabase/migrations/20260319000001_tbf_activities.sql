CREATE TABLE IF NOT EXISTS tbf_activities (
    id TEXT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    organization TEXT NOT NULL DEFAULT '',
    city TEXT NOT NULL DEFAULT '',
    discipline TEXT NOT NULL,
    activity_type TEXT NOT NULL,
    detail_url TEXT DEFAULT '',
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tbf_activities_start_date ON tbf_activities(start_date);
CREATE INDEX IF NOT EXISTS idx_tbf_activities_discipline ON tbf_activities(discipline);
ALTER TABLE tbf_activities ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read tbf_activities" ON tbf_activities FOR SELECT USING (true);
