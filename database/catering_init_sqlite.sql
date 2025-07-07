-- database: catering.db
-- VERSIONE COMPLETAMENTE CORRETTA - Risolve TUTTI gli errori
-- 1) FIRST REMOVE ALL TABLES (in reverse dependency order)
DROP TABLE IF EXISTS `Assignment`;
DROP TABLE IF EXISTS `Tasks`;
DROP TABLE IF EXISTS `SummarySheets`;
DROP TABLE IF EXISTS `ShiftBookings`;
DROP TABLE IF EXISTS `Shifts`;
DROP TABLE IF EXISTS `RecipePreparations`;
DROP TABLE IF EXISTS `UserRoles`;
DROP TABLE IF EXISTS `Services`;
DROP TABLE IF EXISTS `MenuItems`;
DROP TABLE IF EXISTS `MenuFeatures`;
DROP TABLE IF EXISTS `MenuSections`;
DROP TABLE IF EXISTS `Menus`;
DROP TABLE IF EXISTS `Events`;
DROP TABLE IF EXISTS `Preparations`;
DROP TABLE IF EXISTS `Recipes`;
DROP TABLE IF EXISTS `Roles`;
DROP TABLE IF EXISTS `HolidayRequestDays`;
DROP TABLE IF EXISTS `HolidayRequests`;
DROP TABLE IF EXISTS `EventRoles`;
DROP TABLE IF EXISTS `EventStaffSheets`;
DROP TABLE IF EXISTS `StaffHolidays`;
DROP TABLE IF EXISTS `EvaluationNotes`;
DROP TABLE IF EXISTS `Users`;  
DROP TABLE IF EXISTS `Staff`;

-- 2) CREATE ALL TABLES 

-- Independent tables first
CREATE TABLE `Roles` (
    `id` INTEGER NOT NULL,
    `role` TEXT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `Recipes` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT,
    `description` TEXT DEFAULT ''
);

CREATE TABLE `Preparations` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT,
    `description` TEXT DEFAULT ''
);

-- Staff table 
CREATE TABLE `Staff` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT NOT NULL,
    `contact` TEXT,
    `address` TEXT,
    `fiscal_code` TEXT,
    `permanent` INTEGER DEFAULT 0,
    `job_type` TEXT NOT NULL CHECK (job_type IN ('COOK', 'CHEF', 'SERVICE')),
    `remaining_days_off` INTEGER DEFAULT 30
);

-- Users table 
CREATE TABLE `Users` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `username` TEXT NOT NULL DEFAULT '', 
    `staff_id` INTEGER DEFAULT NULL,
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE SET NULL
);

-- Tables with simple dependencies
CREATE TABLE `Events` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT,
    `date_start` DATE,
    `date_end` DATE,
    `chef_id` INTEGER NOT NULL
);

CREATE TABLE `Menus` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `title` TEXT,
    `owner_id` INTEGER,
    `published` INTEGER DEFAULT 0
);

CREATE TABLE `MenuSections` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `menu_id` INTEGER NOT NULL,
    `name` TEXT,
    `position` INTEGER
);

CREATE TABLE `UserRoles` (
    `user_id` INTEGER NOT NULL,
    `role_id` INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE `RecipePreparations` (
    `recipe_id` INTEGER,
    `preparation_id` INTEGER,
    PRIMARY KEY (`recipe_id`, `preparation_id`),
    FOREIGN KEY (`recipe_id`) REFERENCES `Recipes` (`id`),
    FOREIGN KEY (`preparation_id`) REFERENCES `Preparations` (`id`)
);

CREATE TABLE `MenuFeatures` (
    `menu_id` INTEGER NOT NULL,
    `name` TEXT NOT NULL DEFAULT '',
    `value` INTEGER DEFAULT 0
);

CREATE TABLE `MenuItems` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `menu_id` INTEGER NOT NULL,
    `section_id` INTEGER,
    `description` TEXT,
    `recipe_id` INTEGER NOT NULL,
    `position` INTEGER
);

CREATE TABLE `Services` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `event_id` INTEGER NOT NULL,
    `name` TEXT,
    `approved_menu_id` INTEGER DEFAULT 0,
    `service_date` DATE,
    `time_start` TIME,
    `time_end` TIME,
    `location` TEXT
);

CREATE TABLE `Shifts` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `date` DATE NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL
);

CREATE TABLE `ShiftBookings` (
    `shift_id` INTEGER NOT NULL,
    `user_id` INTEGER NOT NULL,
    PRIMARY KEY (`shift_id`, `user_id`)
);

CREATE TABLE `SummarySheets` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `service_id` INTEGER NOT NULL,
    `owner_id` INTEGER NOT NULL,
    FOREIGN KEY (`service_id`) REFERENCES `Services` (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES `Users` (`id`)
);

CREATE TABLE `Tasks` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `sumsheet_id` INTEGER NOT NULL,
    `kitchenproc_id` INTEGER NOT NULL,
    `preparation_id` INTEGER DEFAULT NULL,
    `description` TEXT,
    `type` INTEGER NOT NULL DEFAULT 1,
    `quantity` REAL DEFAULT NULL,
    `position` INTEGER NOT NULL DEFAULT 0,
    `portions` INTEGER DEFAULT NULL,
    `ready` INTEGER DEFAULT 0,
    FOREIGN KEY (`sumsheet_id`) REFERENCES `SummarySheets` (`id`),
    FOREIGN KEY (`kitchenproc_id`) REFERENCES `Recipes` (`id`)
);

CREATE TABLE `Assignment` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `sumsheet_id` INTEGER NOT NULL,
    `task_id` INTEGER NOT NULL,
    `cook_id` INTEGER NOT NULL,
    `shift_id` INTEGER NOT NULL,
    FOREIGN KEY (`sumsheet_id`) REFERENCES `SummarySheets` (`id`),
    FOREIGN KEY (`task_id`) REFERENCES `Tasks` (`id`),
    FOREIGN KEY (`cook_id`) REFERENCES `Users` (`id`),
    FOREIGN KEY (`shift_id`) REFERENCES `Shifts` (`id`)
);

-- ===== STAFF MANAGEMENT EXTENSION TABLES =====

CREATE TABLE `EvaluationNotes` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `staff_id` INTEGER NOT NULL,
    `event_id` INTEGER NOT NULL,
    `note` TEXT NOT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`event_id`) REFERENCES `Events` (`id`) ON DELETE CASCADE
);

CREATE TABLE `StaffHolidays` (
    `staff_id` INTEGER NOT NULL,
    `holiday_date` DATE NOT NULL,
    PRIMARY KEY (`staff_id`, `holiday_date`),
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE
);

CREATE TABLE `EventStaffSheets` (
    `id` INTEGER PRIMARY KEY,
    `note` TEXT DEFAULT '',
    FOREIGN KEY (`id`) REFERENCES `Events` (`id`) ON DELETE CASCADE
);

CREATE TABLE `EventRoles` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `event_id` INTEGER NOT NULL,
    `description` TEXT NOT NULL,
    `job_type` TEXT NOT NULL CHECK (job_type IN ('COOK', 'CHEF', 'SERVICE')),
    `staff_id` INTEGER DEFAULT NULL,
    FOREIGN KEY (`event_id`) REFERENCES `Events` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE SET NULL
);

CREATE TABLE `HolidayRequests` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `staff_id` INTEGER NOT NULL,
    `request_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `description` TEXT DEFAULT '',
    FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`) ON DELETE CASCADE
);

CREATE TABLE `HolidayRequestDays` (
    `request_id` INTEGER NOT NULL,
    `requested_date` DATE NOT NULL,
    PRIMARY KEY (`request_id`, `requested_date`),
    FOREIGN KEY (`request_id`) REFERENCES `HolidayRequests` (`id`) ON DELETE CASCADE
);

-- ===== INDICES FOR PERFORMANCE =====
CREATE INDEX IF NOT EXISTS `idx_staff_job_type` ON `Staff` (`job_type`);
CREATE INDEX IF NOT EXISTS `idx_staff_permanent` ON `Staff` (`permanent`);
CREATE INDEX IF NOT EXISTS `idx_evaluation_notes_staff` ON `EvaluationNotes` (`staff_id`);
CREATE INDEX IF NOT EXISTS `idx_evaluation_notes_event` ON `EvaluationNotes` (`event_id`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_event` ON `EventRoles` (`event_id`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_type` ON `EventRoles` (`job_type`);
CREATE INDEX IF NOT EXISTS `idx_event_roles_staff` ON `EventRoles` (`staff_id`);
CREATE INDEX IF NOT EXISTS `idx_holiday_requests_staff` ON `HolidayRequests` (`staff_id`);

-- ===== DATI DI ESEMPIO =====

-- Clean up existing data
DELETE FROM RecipePreparations WHERE recipe_id > 0;
DELETE FROM Preparations WHERE id > 0;
DELETE FROM Recipes WHERE id > 0;

-- Reset auto-increment counters
DELETE FROM sqlite_sequence WHERE name IN ('Recipes', 'Preparations');

-- Insert Roles PRIMA DI TUTTO
INSERT INTO Roles (id, role) VALUES
(0,'CUOCO'), (1, 'CHEF'), (2, 'ORGANIZZATORE'), (3, 'SERVIZIO'), (4, 'PROPRIETARIO');

-- Insert Staff PRIMA degli Users
INSERT INTO Staff (name, contact, address, fiscal_code, permanent, job_type, remaining_days_off) VALUES 
('Marco Rossi', '333-1234567', 'Via Roma 123, Milano', 'RSSMRA85M01F205X', 1, 'SERVICE', 15),
('Giulia Bianchi', '333-2345678', 'Via Garibaldi 45, Milano', 'BNCLCU90F45F205Y', 1, 'SERVICE', 10),
('Luca Verdi', '333-3456789', 'Corso Italia 67, Milano', 'VRDGRG88H15F205Z', 0, 'COOK', 8),
('Sofia Neri', '333-4567890', 'Via Montenapoleone 12, Milano', 'NRIFRN92D55F205W', 1, 'COOK', 20),
('Antonio Blu', '333-5678901', 'Via Brera 34, Milano', 'BLUALS89L20F205V', 0, 'CHEF', 5),
('Chiara Rosa', '333-6789012', 'Via Torino 78, Milano', 'RSOCRL87A60F205U', 1, 'CHEF', 20),
('Giovanni Gialli', '333-7890123', 'Piazza Duomo 1, Milano', 'GLLGNN85B30F205T', 1, 'SERVICE', 12);

-- Insert Users CON I NOMI DI COLONNA CORRETTI
INSERT INTO Users (username, staff_id) VALUES 
('Marco', 1), ('Giulia', 2), ('Luca', 3), ('Sofia', 4), ('Antonio', 5), ('Chiara', 6), ('Giovanni', 7), ('Francesca', NULL);

-- Assign roles to users CON ID CORRETTI
INSERT INTO UserRoles (user_id, role_id) VALUES
(1, 3), (2, 3), (3, 0), (4, 0), (5, 1), (6, 1), (7, 2), (8, 2),
(7, 3), (6, 2), (8, 4);

-- Insert Preparations
INSERT INTO Preparations (name, description) VALUES
    ('Impasto per pasta', 'Preparazione base per pasta fresca'),
    ('Impasto per pizza', 'Impasto lievitato per pizza'),
    ('Ragù alla bolognese', 'Sugo di carne tipico emiliano'),
    ('Besciamella', 'Salsa bianca a base di burro, farina e latte'),
    ('Pasta sfoglia', 'Impasto a strati per preparazioni dolci e salate'),
    ('Soffritto', 'Base aromatica di cipolla, carota e sedano'),
    ('Brodo vegetale', 'Liquido di cottura a base di verdure'),
    ('Brodo di carne', 'Liquido di cottura a base di carne'),
    ('Salsa di pomodoro', 'Sugo base di pomodoro e basilico'),
    ('Crema pasticcera', 'Crema dolce per ripieni di pasticceria'),
    ('Sugo all''amatriciana', 'Sugo a base di guanciale, pomodoro e pecorino'),
    ('Pesto alla genovese', 'Salsa a base di basilico e pinoli'),
    ('Salsa carbonara', 'Condimento a base di uova, guanciale e pecorino'),
    ('Gremolata', 'Condimento aromatico a base di prezzemolo, aglio e limone'),
    ('Marinatura per pesce', 'Preparazione di olio, limone ed erbe per pesce'),
    ('Marinatura per carne', 'Preparazione di erbe, vino e spezie per carne'),
    ('Pangrattato aromatizzato', 'Pane grattugiato con erbe e spezie'),
    ('Crema al mascarpone', 'Base per dolci a base di mascarpone e uova'),
    ('Pasta brisée', 'Impasto base per crostate salate'),
    ('Pasta frolla', 'Impasto dolce per crostate e biscotti');

-- Insert Recipes
INSERT INTO Recipes (name, description) VALUES
    -- ANTIPASTI
    ('Bruschetta al Pomodoro', 'Fette di pane casereccio tostate con pomodoro, aglio, basilico e olio extravergine d''oliva'),
    ('Caprese', 'Insalata con mozzarella di bufala, pomodori e basilico condita con olio e aceto balsamico'),
    ('Arancini di Riso', 'Crocchette di riso ripiene di ragù, piselli e mozzarella, fritte'),
    ('Prosciutto e Melone', 'Fette di melone avvolte con prosciutto crudo'),
    ('Caponata Siciliana', 'Piatto agrodolce siciliano con melanzane, sedano, olive e capperi'),
    -- PRIMI PIATTI
    ('Spaghetti alla Carbonara', 'Pasta condita con uova, guanciale, pecorino romano e pepe nero'),
    ('Risotto ai Funghi Porcini', 'Riso cremoso con funghi porcini e parmigiano'),
    ('Lasagne alla Bolognese', 'Pasta a strati con ragù, besciamella e parmigiano reggiano'),
    ('Tagliatelle al Ragù', 'Pasta all''uovo con tradizionale sugo di carne'),
    ('Pasta alla Norma', 'Pasta con melanzane fritte, pomodoro, ricotta salata e basilico'),
    ('Gnocchi di Patate al Pesto', 'Gnocchi di patate conditi con pesto alla genovese'),
    ('Spaghetti all''Amatriciana', 'Pasta con sugo a base di guanciale, pomodoro e pecorino'),
    -- SECONDI PIATTI
    ('Ossobuco alla Milanese', 'Stinco di vitello brasato con gremolata, servito con risotto giallo'),
    ('Saltimbocca alla Romana', 'Fettine di vitello con prosciutto e salvia'),
    ('Bistecca alla Fiorentina', 'Bistecca di manzo alta con osso cotta sulla griglia'),
    ('Pollo alla Cacciatora', 'Pollo in umido con pomodoro, cipolle, erbe e vino'),
    ('Melanzane alla Parmigiana', 'Melanzane a strati con pomodoro, mozzarella e parmigiano'),
    ('Pesce al Forno con Patate', 'Pesce al forno con patate, pomodorini ed erbe aromatiche'),
    ('Vitello Tonnato', 'Fettine di vitello con salsa al tonno e capperi'),
    -- CONTORNI
    ('Patate al Rosmarino', 'Patate arrosto con rosmarino e aglio'),
    ('Verdure Grigliate', 'Verdure di stagione grigliate e condite con olio d''oliva'),
    ('Insalata Mista', 'Insalata verde con pomodori, carote e cetrioli'),
    ('Fagioli all''Uccelletto', 'Fagioli in umido con salvia e pomodoro'),
    -- DOLCI
    ('Tiramisù', 'Dolce al cucchiaio con mascarpone, caffè e savoiardi'),
    ('Panna Cotta', 'Dolce a base di panna cotta servito con frutta o caramello'),
    ('Cannoli Siciliani', 'Cialde fritte ripiene di crema di ricotta e gocce di cioccolato'),
    ('Gelato Artigianale', 'Gelato in vari gusti fatto con ingredienti freschi'),
    ('Crostata di Frutta', 'Torta con base di pasta frolla, crema pasticcera e frutta fresca'),
    -- PIZZE
    ('Pizza Margherita', 'Pizza classica con pomodoro, mozzarella e basilico'),
    ('Pizza Quattro Formaggi', 'Pizza con mozzarella, gorgonzola, fontina e parmigiano'),
    ('Pizza Diavola', 'Pizza piccante con salame piccante'),
    ('Pizza Marinara', 'Pizza semplice con pomodoro, aglio e origano'),
    -- SPECIALITÀ REGIONALI
    ('Polenta con Funghi', 'Polenta cremosa servita con funghi trifolati'),
    ('Ribollita Toscana', 'Zuppa di pane toscana con verdure e fagioli'),
    ('Arrosticini Abruzzesi', 'Spiedini di carne di pecora alla brace tipici dell''Abruzzo'),
    ('Canederli Tirolesi', 'Gnocchi di pane del Trentino-Alto Adige'),
    ('Bagna Càuda Piemontese', 'Salsa calda a base di aglio, acciughe e olio con verdure crude'),
    ('Risotto alla Milanese', 'Risotto allo zafferano tipico milanese'),
    ('Pesto alla Trapanese', 'Pesto siciliano con pomodoro, mandorle e basilico');

-- Insert Recipe-Preparation relationships
INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p
WHERE r.name = 'Bruschetta al Pomodoro' AND p.name = 'Salsa di pomodoro';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p
WHERE r.name = 'Spaghetti alla Carbonara' AND p.name = 'Salsa carbonara';

INSERT INTO RecipePreparations (recipe_id, preparation_id)
SELECT r.id, p.id FROM Recipes r, Preparations p
WHERE r.name = 'Lasagne alla Bolognese' AND p.name IN ('Ragù alla bolognese', 'Besciamella', 'Impasto per pasta');

-- Create sample menu - Francesca è owner!
INSERT INTO Menus (title, owner_id, published) VALUES ('Menu Gala Aziendale', 8, 1);

-- Add sections to menu
INSERT INTO MenuSections (menu_id, name, position) VALUES 
(1, 'Antipasti', 0), (1, 'Primi Piatti', 1), (1, 'Secondi Piatti', 2), (1, 'Dolci', 3);

-- Add menu items
INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 1, 'Caprese', r.id, 0 FROM Recipes r WHERE r.name = 'Caprese';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 1, 'Prosciutto e Melone', r.id, 1 FROM Recipes r WHERE r.name = 'Prosciutto e Melone';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 2, 'Spaghetti alla Carbonara', r.id, 0 FROM Recipes r WHERE r.name = 'Spaghetti alla Carbonara';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 3, 'Ossobuco alla Milanese', r.id, 0 FROM Recipes r WHERE r.name = 'Ossobuco alla Milanese';

INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) 
SELECT 1, 4, 'Tiramisù', r.id, 0 FROM Recipes r WHERE r.name = 'Tiramisù';

-- Create sample event
INSERT INTO Events (name, date_start, date_end, chef_id) VALUES 
('Gala Aziendale Annuale', '2025-06-15', '2025-06-16', 5);

-- Create services for the event
INSERT INTO Services (event_id, name, approved_menu_id, service_date, time_start, time_end, location) VALUES 
(1, 'Pranzo Buffet Aziendale', 1, '2025-06-15', '12:00:00', '15:00:00', 'Salone Grande'),
(1, 'Cena di Gala', 0, '2025-06-15', '19:00:00', '23:00:00', 'Sala Esecutiva');

-- Insert sample holidays already taken
INSERT INTO StaffHolidays (staff_id, holiday_date) VALUES 
(1, '2025-04-15'), (1, '2025-04-16'), (1, '2025-04-17'),
(2, '2025-03-10'), (2, '2025-03-11'), (2, '2025-04-25'), (2, '2025-04-28'),
(3, '2025-05-01'),
(4, '2025-02-14'), (4, '2025-02-15'), (4, '2025-02-16'), (4, '2025-06-02'),
(6, '2025-01-20'), (6, '2025-01-21'), (6, '2025-01-22');

-- Create EventStaffSheet for the existing event
INSERT INTO EventStaffSheets (id, note) VALUES 
(1, 'Evento importante - richiedere massima professionalità dal personale di sala');

-- Insert roles for the event
INSERT INTO EventRoles (event_id, description, job_type, staff_id) VALUES 
(1, 'Chef responsabile', 'CHEF', 5),
(1, 'Sous Chef', 'CHEF', 6),
(1, 'Cuoco principale antipasti', 'COOK', 3),
(1, 'Cuoco principale primi', 'COOK', 4),
(1, 'Responsabile sala', 'SERVICE', 1),
(1, 'Cameriere sala principale', 'SERVICE', 2),
(1, 'Addetto bar', 'SERVICE', 7);

-- Insert sample holiday requests
INSERT INTO HolidayRequests (staff_id, description) VALUES 
(4, 'Matrimonio fratello'),
(5, 'Vacanza estiva'),
(2, 'Ferie natalizie'),
(6, 'Settimana di riposo');

-- Insert requested days for holiday requests
INSERT INTO HolidayRequestDays (request_id, requested_date) VALUES 
-- Request 1: Sofia Neri - matrimonio fratello
(1, '2025-07-12'), (1, '2025-07-13'), (1, '2025-07-14'),
-- Request 2: Antonio Blu - vacanza estiva  
(2, '2025-08-10'), (2, '2025-08-11'), (2, '2025-08-12'), (2, '2025-08-14'), (2, '2025-08-15'), (2, '2025-08-16'),
-- Request 3: Giulia Bianchi - ferie natalizie
(3, '2025-12-23'), (3, '2025-12-24'), (3, '2025-12-27'), (3, '2025-12-30'), (3, '2025-12-31'),
-- Request 4: Chiara Rosa - settimana riposo
(4, '2025-09-15'), (4, '2025-09-16'), (4, '2025-09-17'), (4, '2025-09-18'), (4, '2025-09-19');

-- Create shifts for the event
INSERT INTO Shifts (date, start_time, end_time) VALUES
('2025-06-15', '08:00:00', '16:00:00'),
('2025-06-15', '16:00:00', '23:59:59'),
('2025-06-16', '08:00:00', '16:00:00');