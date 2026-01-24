INSERT INTO room (id, location, manager_id) VALUES
(1, 'Гостиная', (SELECT id FROM users WHERE username = 'user')),
(2, 'Кухня', (SELECT id FROM users WHERE username = 'user')),
(3, 'Спальня', (SELECT id FROM users WHERE username = 'admin')),
(4, 'Ванная комната', (SELECT id FROM users WHERE username = 'user')),
(5, 'Кабинет', (SELECT id FROM users WHERE username = 'admin')),
(6, 'Детская', (SELECT id FROM users WHERE username = 'user')),
(7, 'Прихожая', (SELECT id FROM users WHERE username = 'user')),
(8, 'Балкон', (SELECT id FROM users WHERE username = 'admin'))
ON CONFLICT (id) DO NOTHING;

-- 2. Режимы
INSERT INTO mode (id, mode_type) VALUES
(1, 'MORNING'),
(2, 'DINNER'),
(3, 'STUDY')
ON CONFLICT (id) DO NOTHING;

-- 3. Правила режимов
INSERT INTO mode_rule (id, mode_type, device_type, title_pattern, min_power, max_power, should_be_active, priority) VALUES
(1, 'MORNING', 'COFFEE_MACHINE', NULL, NULL, NULL, true, 10),
(2, 'MORNING', 'LIGHT', '.*утренний.*|.*будильник.*|.*утро.*', NULL, NULL, true, 9),
(3, 'MORNING', 'KETTLE', NULL, NULL, NULL, true, 8),
(4, 'MORNING', 'TELEVISION', NULL, NULL, NULL, false, 5),
(5, 'DINNER', 'LIGHT', '.*обеден.*|.*кухн.*|.*стол.*', NULL, NULL, true, 10),
(6, 'DINNER', 'MICROWAVE', NULL, NULL, NULL, true, 9),
(7, 'DINNER', 'KETTLE', NULL, NULL, NULL, true, 8),
(8, 'DINNER', 'TELEVISION', NULL, NULL, NULL, false, 4),
(9, 'STUDY', 'LIGHT', '.*учебный.*|.*кабинет.*|.*рабочий.*|.*настольн.*', NULL, NULL, true, 10),
(10, 'STUDY', 'SPEAKERS', NULL, NULL, 100.0, true, 7),
(11, 'STUDY', 'COFFEE_MACHINE', NULL, NULL, NULL, true, 6),
(12, 'STUDY', 'TELEVISION', NULL, NULL, NULL, false, 3)
ON CONFLICT (id) DO NOTHING;

-- 4. Устройства
INSERT INTO device (id, title, type, power, active, room_id) VALUES
(1, 'Основной свет гостиной', 'LIGHT', 80.0, true, 1),
(2, 'Торшер угловой', 'LIGHT', 40.0, false, 1),
(3, 'Телевизор Smart TV', 'TELEVISION', 120.0, true, 1),
(4, 'Кондиционер гостиной', 'CONDITIONER', 1500.0, false, 1),
(5, 'Музыкальный центр', 'SPEAKERS', 60.0, false, 1),
(6, 'Свет над столом', 'LIGHT', 50.0, true, 2),
(7, 'Холодильник', 'MICROWAVE', 200.0, true, 2),
(8, 'Электрический чайник', 'KETTLE', 2200.0, false, 2),
(9, 'Кофемашина Delonghi', 'COFFEE_MACHINE', 1450.0, true, 2),
(10, 'Микроволновка Samsung', 'MICROWAVE', 900.0, false, 2),
(11, 'Вытяжка', 'LIGHT', 30.0, true, 2),
(12, 'Прикроватная лампа', 'LIGHT', 25.0, false, 3),
(13, 'Кондиционер спальный', 'CONDITIONER', 1200.0, true, 3),
(14, 'Телевизор в спальне', 'TELEVISION', 100.0, false, 3),
(15, 'Утренний будильник-свет', 'LIGHT', 20.0, false, 3),
(16, 'Свет в ванной', 'LIGHT', 40.0, true, 4),
(17, 'Фен', 'KETTLE', 1800.0, false, 4),
(18, 'Полотенцесушитель', 'KETTLE', 500.0, true, 4),
(19, 'Настольная лампа', 'LIGHT', 35.0, true, 5),
(20, 'Компьютерные колонки', 'SPEAKERS', 45.0, false, 5),
(21, 'Учебный свет', 'LIGHT', 60.0, true, 5),
(22, 'Кофемашина мини', 'COFFEE_MACHINE', 800.0, false, 5),
(23, 'Ночник', 'LIGHT', 5.0, true, 6),
(24, 'Ночник проектор', 'LIGHT', 10.0, false, 6),
(25, 'Радионяня', 'SPEAKERS', 15.0, true, 6),
(26, 'Свет в прихожей', 'LIGHT', 30.0, true, 7),
(27, 'Зеркало с подсветкой', 'LIGHT', 20.0, false, 7),
(28, 'Балконный свет', 'LIGHT', 25.0, false, 8),
(29, 'Обогреватель балкона', 'KETTLE', 2000.0, false, 8),
(30, 'Переносной вентилятор', 'CONDITIONER', 80.0, true, NULL),
(31, 'Портативная колонка', 'SPEAKERS', 25.0, false, NULL),
(32, 'Запасной чайник', 'KETTLE', 2100.0, false, NULL)
ON CONFLICT (id) DO NOTHING;

-- 5. Связь режимов и устройств
INSERT INTO mode_device (mode_id, device_id) VALUES
(1, 9),   -- Кофемашина на кухне
(1, 15),  -- Утренний будильник-свет
(1, 8),   -- Чайник на кухне
(2, 6),   -- Свет над столом
(2, 10),  -- Микроволновка
(2, 8),   -- Чайник
(3, 19),  -- Настольная лампа
(3, 20),  -- Компьютерные колонки
(3, 21),  -- Учебный свет
(3, 22)   -- Кофемашина мини
ON CONFLICT DO NOTHING;