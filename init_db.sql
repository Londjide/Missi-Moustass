-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'user'
);

-- Table des enregistrements audio
CREATE TABLE IF NOT EXISTS recordings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    duration INTEGER,
    audio BLOB,
    encryption_key TEXT,
    audio_hash TEXT,
    user_id INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(id)
);

-- Insertion des utilisateurs par défaut
INSERT OR IGNORE INTO users (email, password, role) VALUES ('admin@missie.com', 'StrongP@ss143', 'admin');
INSERT OR IGNORE INTO users (email, password, role) VALUES ('user@missie.com', 'StrongP@ss143', 'user');