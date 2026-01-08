-- Crea il database
CREATE DATABASE IF NOT EXISTS `library` 
/*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ 
/*!80016 DEFAULT ENCRYPTION='N' */;

USE `library`;

-- Tabella categorie (deve essere creata prima di books per il foreign key)
CREATE TABLE IF NOT EXISTS `categories` (
  `category` varchar(50) NOT NULL,
  PRIMARY KEY (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella books
CREATE TABLE IF NOT EXISTS `books` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `author` varchar(255) NOT NULL,
  `year` int DEFAULT NULL,
  `plot` text,
  `image_path` varchar(255) DEFAULT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `pages` int DEFAULT NULL,
  `isbn` varchar(50) DEFAULT NULL,
  `stock` int DEFAULT '0',
  `category` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `fk_books_category` (`category`),
  CONSTRAINT `fk_books_category` FOREIGN KEY (`category`) REFERENCES `categories` (`category`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella users
CREATE TABLE IF NOT EXISTS `users` (
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `role` enum('admin','logged_user') NOT NULL DEFAULT 'logged_user',
  PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella posts
CREATE TABLE IF NOT EXISTS `posts` (
  `user_fk` varchar(100) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `content` text NOT NULL,
  `post_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_fk`,`post_date`),
  CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_fk`) REFERENCES `users` (`email`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella loans
CREATE TABLE IF NOT EXISTS `loans` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_email` varchar(100) NOT NULL,
  `book_id` int NOT NULL,
  `status` enum('RESERVED','LOANED','EXPIRED','RETURNED') NOT NULL DEFAULT 'RESERVED',
  `reserved_date` date DEFAULT NULL,
  `loaned_date` date DEFAULT NULL,
  `returning_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_email` (`user_email`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `loans_ibfk_1` FOREIGN KEY (`user_email`) REFERENCES `users` (`email`) ON DELETE CASCADE,
  CONSTRAINT `loans_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella purchases
CREATE TABLE `purchases` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_email` varchar(100) NOT NULL,
  `book_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `status` enum('RESERVED','PURCHASED') NOT NULL DEFAULT 'RESERVED',
  `status_date` date NOT NULL DEFAULT (curdate()),
  PRIMARY KEY (`id`),
  KEY `user_email` (`user_email`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `purchases_ibfk_1` FOREIGN KEY (`user_email`) REFERENCES `users` (`email`) ON DELETE CASCADE,
  CONSTRAINT `purchases_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabella wishlist
CREATE TABLE IF NOT EXISTS `wishlist` (
  `user_email` varchar(100) NOT NULL,
  `book_id` int NOT NULL,
  PRIMARY KEY (`user_email`,`book_id`),
  UNIQUE KEY `unique_wishlist` (`user_email`,`book_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `wishlist_ibfk_1` FOREIGN KEY (`user_email`) REFERENCES `users` (`email`) ON DELETE CASCADE,
  CONSTRAINT `wishlist_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELIMITER $$

-- Evento aggiornamento giornaliero
CREATE EVENT IF NOT EXISTS `daily_cleanup_tasks`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
ON COMPLETION PRESERVE
ENABLE
DO
BEGIN
    -- 1. Aggiorna i prestiti scaduti (LOANED -> EXPIRED)
    UPDATE loans 
    SET status = 'EXPIRED' 
    WHERE status = 'LOANED' AND returning_date < CURRENT_DATE;
    
    -- 2. Elimina i prestiti RESERVED oltre 14 giorni
    DELETE FROM loans 
    WHERE status = 'RESERVED' AND reserved_date < DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY);
    
    -- 3. Elimina gli acquisti RESERVED oltre 14 giorni
    DELETE FROM purchases 
    WHERE status = 'RESERVED' AND status_date < DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY);
END$$

DELIMITER ;