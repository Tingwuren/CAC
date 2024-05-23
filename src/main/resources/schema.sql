DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(255) NOT NULL,
    id_number VARCHAR(255) NOT NULL
);

DROP TABLE IF EXISTS `request`;

CREATE TABLE `request` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `room_id` VARCHAR(255) NOT NULL,
    `count` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `state` VARCHAR(255) NOT NULL,
    `fan_speed` VARCHAR(255) NOT NULL,
    `duration` VARCHAR(255) NOT NULL,
    `start_time` VARCHAR(255) NOT NULL,
    `end_time` VARCHAR(255) NOT NULL,
    `start_temp` DOUBLE NOT NULL,
    `end_temp` DOUBLE NOT NULL,
    `energy` DOUBLE NOT NULL,
    `cost` DOUBLE NOT NULL
);