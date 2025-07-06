-- Estensione database CatERing per modulo Staff Management
-- Questo file aggiunge le tabelle necessarie per la gestione del personale e degli eventi
-- Eseguire DOPO catering_init_sqlite.sql
-- 
-- Design principale:
-- - EventStaffSheet ha List<Role> assignments
-- - Role ha Staff assignedStaff (nullable)
-- - Relazione 1:1 tra Event e EventStaffSheet (chiave primaria condivisa)

-- ===== TABELLE PRINCIPALI =====

-- Tabella Staff per la gestione del personale
CREATE TABLE IF NOT EXISTS `Staff` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT NOT NULL,
    `contact` TEXT,
    `address` TEXT,
    `fiscal_code` TEXT, -- Codice Fiscale
    `permanent` INTEGER DEFAULT 0, -- boolean: 0=temporaneo, 1=permanente
    `job_type` TEXT NOT NULL CHECK (job_type IN ('CUOCO', 'CHEF', 'SERVIZIO'), -- tipo contrattuale
    `remaining_days_off` INTEGER DEFAULT 30 -- giorni di ferie rimanenti
);

-- Tabella per le note di valutazione del personale
CREATE TABLE IF NOT EXISTS `EvaluationNotes` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `staff_id` INTEGER NOT NULL,
    `event_id` INTEGER NOT NULL,
    `note` TEXT NOT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`event_id`) REFERENCES `Events` (`id`) ON DELETE CASCADE
);

-- Tabella per le singole date di ferie già prese dal personale (Set<Date> holidays)
CREATE TABLE IF NOT EXISTS `StaffHolidays` (
    `staff_id` INTEGER NOT NULL,
    `holiday_date` DATE NOT NULL,
    PRIMARY KEY (`staff_id`, `holiday_date`),
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE
);

-- Tabella EventStaffSheets con chiave primaria condivisa con Events (1:1)
CREATE TABLE IF NOT EXISTS `EventStaffSheets` (
    `id` INTEGER PRIMARY KEY, -- Stesso ID dell'evento corrispondente
    `note` TEXT DEFAULT '', -- Note generali sulla gestione staff dell'evento
    FOREIGN KEY (`id`) REFERENCES `Events` (`id`) ON DELETE CASCADE
);

-- Tabella per i ruoli operativi definiti per ciascun evento
-- Ogni Role può avere o non avere uno Staff assegnato (assignedStaff può essere null)
CREATE TABLE IF NOT EXISTS `EventRoles` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `event_id` INTEGER NOT NULL,
    `description` TEXT NOT NULL, -- es. "Addetto alla griglia", "Responsabile sala"
    `job_type` TEXT NOT NULL CHECK (job_type IN ('CUOCO', 'CHEF', 'SERVIZIO'), -- qualifica richiesta
    `staff_id` INTEGER DEFAULT NULL, -- Staff assegnato (NULL = non assegnato)
    FOREIGN KEY (`event_id`) REFERENCES `Events` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE SET NULL
);

-- Tabella per le richieste di ferie del personale
-- Le richieste vengono ELIMINATE dopo essere state processate:
-- - Se ACCETTATE: si aggiungono le date in StaffHolidays, poi si elimina la richiesta
-- - Se RIFIUTATE: si elimina direttamente la richiesta
CREATE TABLE IF NOT EXISTS `HolidayRequests` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `staff_id` INTEGER NOT NULL,
    `description` TEXT DEFAULT '',
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE
);

-- Tabella per i giorni specifici richiesti in ogni richiesta ferie (Set<Date> requestedDays)
CREATE TABLE IF NOT EXISTS `HolidayRequestDays` (
    `request_id` INTEGER NOT NULL,
    `requested_date` DATE NOT NULL,
    PRIMARY KEY (`request_id`, `requested_date`),
    FOREIGN KEY (`request_id`) REFERENCES `HolidayRequests` (`id`) ON DELETE CASCADE
);

-- ===== INDICI PER PERFORMANCE =====

CREATE INDEX IF NOT EXISTS `idx_staff_job_type` ON `Staff` (`job_type`);
CREATE INDEX IF NOT EXISTS `idx_staff_permanent` ON `Staff` (`permanent`);
CREATE INDEX IF NOT EXISTS `idx_evaluation_notes_staff` ON `EvaluationNotes` (`staff_id`);
CREATE INDEX IF NOT EXISTS `idx_evaluation_notes_event` ON `EvaluationNotes` (`event_id`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_event` ON `EventRoles` (`event_id`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_type` ON `EventRoles` (`job_type`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_staff` ON `EventRoles` (`staff_id`);
CREATE INDEX IF NOT EXISTS `idx_holiday_requests_staff` ON `HolidayRequests` (`staff_id`);
CREATE INDEX IF NOT EXISTS `idx_holiday_requests_status` ON `HolidayRequests` (`status`);

-- ===== DATI DI ESEMPIO =====

-- Inserimento staff di esempio
INSERT INTO `Staff` (`name`, `contact`, `address`, `fiscal_code`, `permanent`, `job_type`, `remaining_days_off`) VALUES 
('Mario Rossi', '333-1234567', 'Via Roma 123, Milano', 'RSSMRA85M01F205X', 1, 'CHEF', 15),
('Lucia Bianchi', '333-2345678', 'Via Garibaldi 45, Milano', 'BNCLCU90F45F205Y', 1, 'CUOCO', 12),
('Giorgio Verdi', '333-3456789', 'Corso Italia 67, Milano', 'VRDGRG88H15F205Z', 0, 'CUOCO', 8),
('Francesca Neri', '333-4567890', 'Via Montenapoleone 12, Milano', 'NRIFRN92D55F205W', 1, 'SERVIZIO', 18),
('Alessandro Blu', '333-5678901', 'Via Brera 34, Milano', 'BLUALS89L20F205V', 0, 'SERVIZIO', 5),
('Carla Rosa', '333-6789012', 'Via Torino 78, Milano', 'RSOCRL87A60F205U', 1, 'CHEF', 20);

-- Ferie già prese 
INSERT INTO `StaffHolidays` (`staff_id`, `holiday_date`, `description`) VALUES 
-- Mario Rossi: ponte pasquale (3 giorni)
(1, '2025-04-15', 'Ponte pasquale'),
(1, '2025-04-16', 'Ponte pasquale'),
(1, '2025-04-17', 'Ponte pasquale'),
-- Lucia Bianchi: weekend lungo + ponte 25 aprile (ex richiesta approvata)
(2, '2025-03-10', 'Weekend lungo'),
(2, '2025-03-11', 'Weekend lungo'),
(2, '2025-04-25', 'Ponte del 25 aprile'),
(2, '2025-04-28', 'Ponte del 25 aprile'),
-- Giorgio Verdi: festa del lavoro
(3, '2025-05-01', 'Festa del lavoro'),
-- Francesca Neri: weekend romantico + festa della Repubblica (ex richiesta approvata)
(4, '2025-02-14', 'Weekend romantico'),
(4, '2025-02-15', 'Weekend romantico'),
(4, '2025-02-16', 'Weekend romantico'),
(4, '2025-06-02', 'Festa della Repubblica'),
-- Carla Rosa: ferie invernali
(6, '2025-01-20', 'Ferie invernali'),
(6, '2025-01-21', 'Ferie invernali'),
(6, '2025-01-22', 'Ferie invernali');

-- Creazione EventStaffSheet per l'evento esistente (ID 1: "Gala Aziendale Annuale")
INSERT INTO `EventStaffSheets` (`id`, `note`) VALUES 
(1, 'Evento importante - richiedere massima professionalità dal personale di sala');

-- Ruoli operativi per l''evento Gala Aziendale (ID 1)
-- Alcuni ruoli sono già assegnati, altri no (staff_id = NULL)
INSERT INTO `EventRoles` (`event_id`, `description`, `job_type`, `staff_id`, `assigned_date`) VALUES 
(1, 'Chef responsabile', 'CHEF', 1, '2025-06-01'), -- Mario Rossi assegnato
(1, 'Sous Chef', 'CHEF', 6, '2025-06-01'), -- Carla Rosa assegnata
(1, 'Cuoco principale antipasti', 'CUOCO', 2, '2025-06-02'), -- Lucia Bianchi assegnata
(1, 'Cuoco principale primi', 'CUOCO', NULL, NULL), -- Non ancora assegnato
(1, 'Cuoco dolci', 'CUOCO', NULL, NULL), -- Non ancora assegnato
(1, 'Responsabile sala', 'SERVIZIO', 4, '2025-06-01'), -- Francesca Neri assegnata
(1, 'Cameriere sala principale', 'SERVIZIO', 5, '2025-06-02'), -- Alessandro Blu assegnato
(1, 'Cameriere sala VIP', 'SERVIZIO', NULL, NULL), -- Non ancora assegnato
(1, 'Addetto bar', 'SERVIZIO', NULL, NULL); -- Non ancora assegnato

-- Richieste ferie di esempio
INSERT INTO `HolidayRequests` (`staff_id`, `description`, `status`) VALUES 
(3, 'Matrimonio fratello'),        -- ID 1
(5, 'Vacanza estiva'),             -- ID 2  
(1, 'Ferie natalizie'),            -- ID 3
(6, 'Settimana di riposo');        -- ID 4

-- Giorni specifici per le richieste ferie
INSERT INTO `HolidayRequestDays` (`request_id`, `requested_date`) VALUES 
-- Richiesta 1: Giorgio Verdi - matrimonio fratello (3 giorni)
(1, '2025-07-12'),
(1, '2025-07-13'), 
(1, '2025-07-14'),
-- Richiesta 2: Alessandro Blu - vacanza estiva (5 giorni non consecutivi)
(2, '2025-08-10'),
(2, '2025-08-11'),
(2, '2025-08-12'),
(2, '2025-08-14'), -- Salta il 13 (mercoledì)
(2, '2025-08-15'),
-- Richiesta 3: Mario Rossi - ferie natalizie (giorni lavorativi)
(3, '2025-12-23'),
(3, '2025-12-24'),
(3, '2025-12-27'), -- Salta weekend 25-26
(3, '2025-12-30'),
(3, '2025-12-31'),
-- Richiesta 4: Carla Rosa - settimana riposo (solo giorni feriali)
(4, '2025-09-15'), -- Lunedì
(4, '2025-09-16'), -- Martedì  
(4, '2025-09-17'), -- Mercoledì
(4, '2025-09-18'), -- Giovedì
(4, '2025-09-19'); -- Venerdì