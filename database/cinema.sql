-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: cinemaweb
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!50503 SET NAMES utf8 */
;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */
;
/*!40103 SET TIME_ZONE='+00:00' */
;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */
;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */
;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */
;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */
;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `booking` (
    `booking_id` int NOT NULL AUTO_INCREMENT,
    `schedule_id` int NOT NULL,
    `price` double NOT NULL,
    `booking_day` datetime(6) DEFAULT NULL,
    `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`booking_id`),
    UNIQUE KEY `UKjx9a9qmksuasu3taidjqyp0co` (`user_id`),
    KEY `fk_booking_schedule_idx` (`schedule_id`),
    CONSTRAINT `fk_booking_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FKkgseyy7t56x7lkjgu3wah5s3t` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */
;
INSERT INTO
    `booking`
VALUES (
        1,
        1,
        3000,
        '2025-01-05 00:00:00.000000',
        'd2733469-36ff-418f-8508-7c3c3a4ed433'
    ),
    (
        2,
        2,
        6000,
        '2025-05-01 11:37:39.178783',
        '4da4ed59-19c8-483e-9fda-fcf1d238a60a'
    );
/*!40000 ALTER TABLE `booking` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `booking_seat`
--

DROP TABLE IF EXISTS `booking_seat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `booking_seat` (
    `id` int NOT NULL AUTO_INCREMENT,
    `booking_id` int DEFAULT NULL,
    `seat_schedule_id` int DEFAULT NULL,
    `price` double DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `booking_id` (`booking_id`),
    KEY `seat_schedule_id` (`seat_schedule_id`),
    CONSTRAINT `booking_seat_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
    CONSTRAINT `booking_seat_ibfk_2` FOREIGN KEY (`seat_schedule_id`) REFERENCES `seat_schedule` (`seat_schedule_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `booking_seat`
--

LOCK TABLES `booking_seat` WRITE;
/*!40000 ALTER TABLE `booking_seat` DISABLE KEYS */
;
/*!40000 ALTER TABLE `booking_seat` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `bookingfoodanddrink`
--

DROP TABLE IF EXISTS `bookingfoodanddrink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `bookingfoodanddrink` (
    `booking_fd_id` int NOT NULL AUTO_INCREMENT,
    `booking_id` int DEFAULT NULL,
    `fd_id` int DEFAULT NULL,
    `quantity` int DEFAULT NULL,
    `price` double DEFAULT NULL,
    PRIMARY KEY (`booking_fd_id`),
    KEY `booking_id` (`booking_id`),
    KEY `fd_id` (`fd_id`),
    CONSTRAINT `bookingfoodanddrink_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
    CONSTRAINT `bookingfoodanddrink_ibfk_2` FOREIGN KEY (`fd_id`) REFERENCES `foodanddrink` (`fd_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `bookingfoodanddrink`
--

LOCK TABLES `bookingfoodanddrink` WRITE;
/*!40000 ALTER TABLE `bookingfoodanddrink` DISABLE KEYS */
;
INSERT INTO `bookingfoodanddrink` VALUES (1, 1, 1, 2, 6000);
/*!40000 ALTER TABLE `bookingfoodanddrink` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `cinema`
--

DROP TABLE IF EXISTS `cinema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `cinema` (
    `cinema_id` int NOT NULL AUTO_INCREMENT,
    `cinema_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `cinema_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`cinema_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `cinema`
--

LOCK TABLES `cinema` WRITE;
/*!40000 ALTER TABLE `cinema` DISABLE KEYS */
;
INSERT INTO
    `cinema`
VALUES (
        1,
        'Beta Xuan Thuy, Cau giay',
        '102 xuan thuy, cau giay'
    ),
    (
        3,
        'Beta Giai Phong',
        '781 giai phong, hai ba trung'
    );
/*!40000 ALTER TABLE `cinema` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `foodanddrink`
--

DROP TABLE IF EXISTS `foodanddrink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `foodanddrink` (
    `fd_id` int NOT NULL AUTO_INCREMENT,
    `fd_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `cinema_id` int NOT NULL,
    `fd_price` double NOT NULL,
    `image_food_and_drink` longtext COLLATE utf8mb4_unicode_ci,
    PRIMARY KEY (`fd_id`),
    KEY `fk_foodanddrink_room_idx` (`cinema_id`),
    CONSTRAINT `FK2bfct4r9wwpgl4ee44p4kydrn` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`cinema_id`),
    CONSTRAINT `fk_foodanddrink_room` FOREIGN KEY (`cinema_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `foodanddrink`
--

LOCK TABLES `foodanddrink` WRITE;
/*!40000 ALTER TABLE `foodanddrink` DISABLE KEYS */
;
INSERT INTO
    `foodanddrink`
VALUES (
        1,
        '1 bong 1 nuoc',
        3,
        3000,
        NULL
    ),
    (
        2,
        '2 bong 1 nuoc',
        3,
        3000,
        NULL
    ),
    (
        4,
        '3 bong 2 nuoc',
        1,
        3000,
        NULL
    ),
    (5, 'Hun nhau', 1, 20, NULL),
    (
        6,
        'HunHunngHunngHunng nhau',
        1,
        202020,
        NULL
    ),
    (
        7,
        'HunHunngHunngHunng nhau',
        1,
        202020,
        NULL
    );
/*!40000 ALTER TABLE `foodanddrink` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `invalidated_token`
--

DROP TABLE IF EXISTS `invalidated_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `invalidated_token` (
    `id` varchar(255) NOT NULL,
    `expiry_time` datetime(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `invalidated_token`
--

LOCK TABLES `invalidated_token` WRITE;
/*!40000 ALTER TABLE `invalidated_token` DISABLE KEYS */
;
/*!40000 ALTER TABLE `invalidated_token` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `movie`
--

DROP TABLE IF EXISTS `movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;

CREATE TABLE `movie` (
    `movie_id` int NOT NULL AUTO_INCREMENT,
    `movie_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_poster` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_genre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `movie_length` int NOT NULL,
    `movie_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `movie_review` double DEFAULT NULL,
    `tmdb_id` int DEFAULT NULL,
    `trailer_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `release_date` date DEFAULT NULL, -- Kiểu DATE trong MySQL tương ứng với LocalDate của Java
    PRIMARY KEY (`movie_id`),
    -- Chống thêm phim trùng: không cho 2 phim cùng tên + cùng ngày phát hành.
    -- Lưu ý: MySQL coi NULL là phân biệt nên release_date phải có giá trị thì ràng buộc mới hiệu lực.
    UNIQUE KEY `uq_movie_name_release_date` (`movie_name`, `release_date`),
    -- Chống import trùng phim từ TMDB (nhiều phim nhập tay có tmdb_id = NULL vẫn hợp lệ).
    UNIQUE KEY `uq_movie_tmdb_id` (`tmdb_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 12 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- Dành cho DATABASE ĐANG CHẠY (đã tạo bảng movie từ trước): chạy thủ công 1 lần.
-- Nếu đang tồn tại 2 dòng cùng movie_name + cùng release_date (không NULL), phải
-- dedupe trước, nếu không lệnh ALTER sẽ báo lỗi "Duplicate entry".
-- ─────────────────────────────────────────────────────────────────────────────
-- ALTER TABLE `movie`
--   ADD UNIQUE KEY `uq_movie_name_release_date` (`movie_name`, `release_date`),
--   ADD UNIQUE KEY `uq_movie_tmdb_id` (`tmdb_id`);

/*!40101 SET character_set_client = @saved_cs_client */
;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `movie`
--

LOCK TABLES `movie` WRITE;
/*!40000 ALTER TABLE `movie` DISABLE KEYS */
;
INSERT INTO
    `movie`
VALUES (
        7,
        'Harry Potter and the Sorcerer\'s Stone',
        'harrypotter1.jpg',
        'Fantasy',
        152,
        'A young wizard discovers his magical heritage and attends Hogwarts School.',
        5
    ),
    (
        8,
        'Test update film',
        'xin chao',
        'Novel',
        180,
        'test spring boot',
        4
    ),
    (
        9,
        'Titanic',
        'titanic.jpg',
        'Romance',
        195,
        'A young couple from different social backgrounds fall in love aboard the ill-fated RMS Titanic.',
        4
    ),
    (
        10,
        'Test update',
        'xin chao',
        'Novel',
        180,
        'test spring boot',
        4
    ),
    (
        11,
        'dmm',
        'Xin chao',
        'Fantasy',
        152,
        'A young wizard discovers his magical heritage and attends Hogwarts School.',
        5
    );
/*!40000 ALTER TABLE `movie` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `password_otp`
--

DROP TABLE IF EXISTS `password_otp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `password_otp` (
    `otp` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expiry_time` datetime(6) NOT NULL,
    `valid` tinyint(1) NOT NULL,
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`otp`),
    KEY `fk_password_otp_user1_idx` (`user_id`),
    CONSTRAINT `fk_password_otp_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `password_otp`
--

LOCK TABLES `password_otp` WRITE;
/*!40000 ALTER TABLE `password_otp` DISABLE KEYS */
;
/*!40000 ALTER TABLE `password_otp` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `permission` (
    `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */
;
/*!40000 ALTER TABLE `permission` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `role` (
    `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */
;
/*!40000 ALTER TABLE `role` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `role_permissions` (
    `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `permission_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (
        `role_name`,
        `permission_name`
    ),
    KEY `fk_role_has_permission_permission1_idx` (`permission_name`),
    KEY `fk_role_has_permission_role1_idx` (`role_name`),
    CONSTRAINT `fk_role_has_permission_permission1` FOREIGN KEY (`permission_name`) REFERENCES `permission` (`name`),
    CONSTRAINT `fk_role_has_permission_role1` FOREIGN KEY (`role_name`) REFERENCES `role` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */
;
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `room` (
    `room_id` int NOT NULL AUTO_INCREMENT,
    `cinema_id` int NOT NULL,
    `room_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `num_col` int NOT NULL,
    `num_row` int NOT NULL,
    PRIMARY KEY (`room_id`),
    KEY `fk_room_cinema_idx` (`cinema_id`),
    CONSTRAINT `fk_room_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`cinema_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */
;
INSERT INTO
    `room`
VALUES (
        1,
        1,
        'Phong chi co hai nguoi thoi',
        0,
        0
    ),
    (
        2,
        1,
        'Test update thu xem chay duoc ko',
        0,
        0
    ),
    (
        3,
        1,
        'Test thu xem chay duoc ko',
        0,
        0
    ),
    (
        5,
        3,
        'Test lan 2 thu xem chay duoc ko',
        0,
        0
    ),
    (
        6,
        1,
        'Test lan 3 thu xem chay duoc ko',
        0,
        0
    ),
    (8, 3, 'Thuong de', 9, 11),
    (9, 3, 'Thuong de', 9, 11);
/*!40000 ALTER TABLE `room` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `schedule`
--

DROP TABLE IF EXISTS `schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `schedule` (
    `schedule_id` int NOT NULL AUTO_INCREMENT,
    `movie_id` int NOT NULL,
    `room_id` int NOT NULL,
    `schedule_date` date NOT NULL,
    `schedule_start` time NOT NULL,
    `schedule_end` time NOT NULL,
    `cinema_id` int DEFAULT NULL,
    PRIMARY KEY (`schedule_id`),
    KEY `fk_schedule_room_idx` (`room_id`),
    KEY `fk_schedule_film_idx` (`movie_id`),
    KEY `FKuk1m56gbrdyomvctf1teeefe` (`cinema_id`),
    CONSTRAINT `fk_schedule_film` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_schedule_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FKuk1m56gbrdyomvctf1teeefe` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`cinema_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 5 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `schedule`
--

LOCK TABLES `schedule` WRITE;
/*!40000 ALTER TABLE `schedule` DISABLE KEYS */
;
INSERT INTO
    `schedule`
VALUES (
        1,
        7,
        1,
        '2005-06-05',
        '12:00:00',
        '14:00:00',
        1
    ),
    (
        2,
        8,
        3,
        '2005-06-05',
        '12:00:00',
        '14:00:00',
        3
    ),
    (
        4,
        7,
        1,
        '2025-05-10',
        '20:30:00',
        '22:30:00',
        1
    );
/*!40000 ALTER TABLE `schedule` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `seat`
--

DROP TABLE IF EXISTS `seat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `seat` (
    `seat_id` int NOT NULL AUTO_INCREMENT,
    `seat_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `room_id` int NOT NULL,
    `seat_row` char(1) COLLATE utf8mb4_unicode_ci NOT NULL,
    `seat_number` int NOT NULL,
    `seat_price` double NOT NULL,
    PRIMARY KEY (`seat_id`),
    KEY `fk_seat_room_idx` (`room_id`),
    CONSTRAINT `fk_seat_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 181 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `seat`
--

LOCK TABLES `seat` WRITE;
/*!40000 ALTER TABLE `seat` DISABLE KEYS */
;
INSERT INTO
    `seat`
VALUES (
        1,
        'Thuong gia',
        1,
        'A',
        25,
        3000
    ),
    (4, 'TiNh iU', 1, 'A', 3, 3000),
    (5, 'Head', 8, 'A', 0, 100000),
    (6, 'Head', 8, 'A', 0, 100000),
    (7, 'Head', 8, 'A', 0, 100000),
    (8, 'Head', 8, 'A', 0, 100000),
    (9, 'Head', 8, 'A', 0, 100000),
    (10, 'Head', 8, 'A', 0, 100000),
    (11, 'Head', 8, 'A', 0, 100000),
    (12, 'Head', 8, 'A', 0, 100000),
    (13, 'Head', 8, 'B', 0, 100000),
    (14, 'Head', 8, 'B', 0, 100000),
    (15, 'Head', 8, 'B', 0, 100000),
    (16, 'Head', 8, 'B', 0, 100000),
    (17, 'Head', 8, 'B', 0, 100000),
    (18, 'Head', 8, 'B', 0, 100000),
    (19, 'Head', 8, 'B', 0, 100000),
    (20, 'Head', 8, 'B', 0, 100000),
    (21, 'Head', 8, 'C', 0, 100000),
    (22, 'Head', 8, 'C', 0, 100000),
    (23, 'Head', 8, 'C', 0, 100000),
    (24, 'Head', 8, 'C', 0, 100000),
    (25, 'Head', 8, 'C', 0, 100000),
    (26, 'Head', 8, 'C', 0, 100000),
    (27, 'Head', 8, 'C', 0, 100000),
    (28, 'Head', 8, 'C', 0, 100000),
    (
        29,
        'Tails',
        8,
        'D',
        0,
        120000
    ),
    (
        30,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        31,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        32,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        33,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        34,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        35,
        'Center',
        8,
        'D',
        0,
        140000
    ),
    (
        36,
        'Tails',
        8,
        'D',
        0,
        120000
    ),
    (
        37,
        'Tails',
        8,
        'E',
        0,
        120000
    ),
    (
        38,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        39,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        40,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        41,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        42,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        43,
        'Center',
        8,
        'E',
        0,
        140000
    ),
    (
        44,
        'Tails',
        8,
        'E',
        0,
        120000
    ),
    (
        45,
        'Tails',
        8,
        'F',
        0,
        120000
    ),
    (
        46,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        47,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        48,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        49,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        50,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        51,
        'Center',
        8,
        'F',
        0,
        140000
    ),
    (
        52,
        'Tails',
        8,
        'F',
        0,
        120000
    ),
    (
        53,
        'Tails',
        8,
        'G',
        0,
        120000
    ),
    (
        54,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        55,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        56,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        57,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        58,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        59,
        'Center',
        8,
        'G',
        0,
        140000
    ),
    (
        60,
        'Tails',
        8,
        'G',
        0,
        120000
    ),
    (
        61,
        'Tails',
        8,
        'H',
        0,
        120000
    ),
    (
        62,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        63,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        64,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        65,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        66,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        67,
        'Center',
        8,
        'H',
        0,
        140000
    ),
    (
        68,
        'Tails',
        8,
        'H',
        0,
        120000
    ),
    (
        69,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        70,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        71,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        72,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        73,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        74,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        75,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        76,
        'Tails',
        8,
        'I',
        0,
        120000
    ),
    (
        77,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        78,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        79,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        80,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        81,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        82,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        83,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        84,
        'Tails',
        8,
        'J',
        0,
        120000
    ),
    (
        85,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        86,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        87,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        88,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        89,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        90,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        91,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        92,
        'Couple',
        8,
        'K',
        0,
        250000
    ),
    (
        93,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        94,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        95,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        96,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        97,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        98,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        99,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        100,
        'Normal',
        9,
        'A',
        0,
        30000
    ),
    (
        101,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        102,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        103,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        104,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        105,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        106,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        107,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        108,
        'Normal',
        9,
        'B',
        0,
        30000
    ),
    (
        109,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        110,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        111,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        112,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        113,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        114,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        115,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (
        116,
        'Normal',
        9,
        'C',
        0,
        30000
    ),
    (117, 'VIP', 9, 'D', 0, 4000),
    (118, 'VIP', 9, 'D', 0, 4000),
    (119, 'VIP', 9, 'D', 0, 4000),
    (120, 'VIP', 9, 'D', 0, 4000),
    (121, 'VIP', 9, 'D', 0, 4000),
    (122, 'VIP', 9, 'D', 0, 4000),
    (123, 'VIP', 9, 'D', 0, 4000),
    (124, 'VIP', 9, 'D', 0, 4000),
    (125, 'VIP', 9, 'E', 0, 4000),
    (126, 'VIP', 9, 'E', 0, 4000),
    (127, 'VIP', 9, 'E', 0, 4000),
    (128, 'VIP', 9, 'E', 0, 4000),
    (129, 'VIP', 9, 'E', 0, 4000),
    (130, 'VIP', 9, 'E', 0, 4000),
    (131, 'VIP', 9, 'E', 0, 4000),
    (132, 'VIP', 9, 'E', 0, 4000),
    (133, 'VIP', 9, 'F', 0, 4000),
    (134, 'VIP', 9, 'F', 0, 4000),
    (135, 'VIP', 9, 'F', 0, 4000),
    (136, 'VIP', 9, 'F', 0, 4000),
    (137, 'VIP', 9, 'F', 0, 4000),
    (138, 'VIP', 9, 'F', 0, 4000),
    (139, 'VIP', 9, 'F', 0, 4000),
    (140, 'VIP', 9, 'F', 0, 4000),
    (141, 'VIP', 9, 'G', 0, 4000),
    (142, 'VIP', 9, 'G', 0, 4000),
    (143, 'VIP', 9, 'G', 0, 4000),
    (144, 'VIP', 9, 'G', 0, 4000),
    (145, 'VIP', 9, 'G', 0, 4000),
    (146, 'VIP', 9, 'G', 0, 4000),
    (147, 'VIP', 9, 'G', 0, 4000),
    (148, 'VIP', 9, 'G', 0, 4000),
    (149, 'VIP', 9, 'H', 0, 4000),
    (150, 'VIP', 9, 'H', 0, 4000),
    (151, 'VIP', 9, 'H', 0, 4000),
    (152, 'VIP', 9, 'H', 0, 4000),
    (153, 'VIP', 9, 'H', 0, 4000),
    (154, 'VIP', 9, 'H', 0, 4000),
    (155, 'VIP', 9, 'H', 0, 4000),
    (156, 'VIP', 9, 'H', 0, 4000),
    (157, 'VIP', 9, 'I', 0, 4000),
    (158, 'VIP', 9, 'I', 0, 4000),
    (159, 'VIP', 9, 'I', 0, 4000),
    (160, 'VIP', 9, 'I', 0, 4000),
    (161, 'VIP', 9, 'I', 0, 4000),
    (162, 'VIP', 9, 'I', 0, 4000),
    (163, 'VIP', 9, 'I', 0, 4000),
    (164, 'VIP', 9, 'I', 0, 4000),
    (165, 'VIP', 9, 'J', 0, 4000),
    (166, 'VIP', 9, 'J', 0, 4000),
    (167, 'VIP', 9, 'J', 0, 4000),
    (168, 'VIP', 9, 'J', 0, 4000),
    (169, 'VIP', 9, 'J', 0, 4000),
    (170, 'VIP', 9, 'J', 0, 4000),
    (171, 'VIP', 9, 'J', 0, 4000),
    (172, 'VIP', 9, 'J', 0, 4000),
    (
        173,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        174,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        175,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        176,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        177,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        178,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        179,
        'Couple',
        9,
        'K',
        0,
        20000
    ),
    (
        180,
        'Couple',
        9,
        'K',
        0,
        20000
    );
/*!40000 ALTER TABLE `seat` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `seat_schedule`
--

DROP TABLE IF EXISTS `seat_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `seat_schedule` (
    `seat_schedule_id` int NOT NULL AUTO_INCREMENT,
    `schedule_id` int NOT NULL,
    `seat_id` int NOT NULL,
    `seat_state` tinyint(1) NOT NULL,
    PRIMARY KEY (`seat_schedule_id`),
    KEY `seat_schedule_ibfk_1` (`schedule_id`),
    KEY `seat_schedule_ibfk_2` (`seat_id`),
    CONSTRAINT `seat_schedule_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`),
    CONSTRAINT `seat_schedule_ibfk_2` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`seat_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `seat_schedule`
--

LOCK TABLES `seat_schedule` WRITE;
/*!40000 ALTER TABLE `seat_schedule` DISABLE KEYS */
;
INSERT INTO `seat_schedule` VALUES (1, 4, 1, 0), (2, 4, 4, 0);
/*!40000 ALTER TABLE `seat_schedule` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `user` (
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `first_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `last_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `date_of_birth` date NOT NULL,
    `gender` int DEFAULT NULL,
    `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `phone_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `point` double DEFAULT '0',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `user_id_UNIQUE` (`user_id`),
    UNIQUE KEY `username_UNIQUE` (`username`),
    UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */
;
INSERT INTO
    `user`
VALUES (
        '4da4ed59-19c8-483e-9fda-fcf1d238a60a',
        'Daolelongvu',
        '$2a$10$ka08fKPXNWi1OcuHjmwzVuZwTqDrnkRm0tsOo.gfmjtiR1nk2TF1W',
        'Dao',
        'Vu',
        '2005-06-05',
        1,
        'default avatar',
        'daolelongvu@gmail.com',
        '0986770920',
        NULL
    );
/*!40000 ALTER TABLE `user` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `user_roles` (
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`user_id`, `role_name`),
    KEY `fk_user_has_role_role1_idx` (`role_name`),
    KEY `fk_user_has_role_user1_idx` (`user_id`),
    CONSTRAINT `fk_user_has_role_role1` FOREIGN KEY (`role_name`) REFERENCES `role` (`name`),
    CONSTRAINT `fk_user_has_role_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */
;
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */
;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */
;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */
;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */
;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */
;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */
;

-- Dump completed on 2025-05-20 23:33:17