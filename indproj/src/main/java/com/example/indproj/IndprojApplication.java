package com.example.indproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения
 * Точка входа в приложение для управления данными о водителях, 
 * автомобилях и штрафах
 */
@SpringBootApplication
public class IndprojApplication {

	/**
	 * Главный метод приложения
	 * Запускает Spring Boot приложение
	 * 
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		SpringApplication.run(IndprojApplication.class, args);
	}
}
