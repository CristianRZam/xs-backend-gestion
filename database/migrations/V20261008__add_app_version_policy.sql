CREATE TABLE IF NOT EXISTS app_version_policies (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL UNIQUE,
    latest_build INTEGER NOT NULL,
    latest_version VARCHAR(40) NOT NULL,
    allowed_builds VARCHAR(500) NOT NULL,
    minimum_build INTEGER NOT NULL,
    download_url VARCHAR(500),
    update_message VARCHAR(500)
);

INSERT INTO app_version_policies
    (platform, latest_build, latest_version, allowed_builds, minimum_build, download_url, update_message)
VALUES
    ('ANDROID', 1, '1.0.0', '1', 1, NULL, NULL)
ON CONFLICT (platform) DO NOTHING;
