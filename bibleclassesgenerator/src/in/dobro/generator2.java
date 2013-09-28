package in.dobro;

import java.sql.SQLException;
import java.util.Scanner;
import java.io.*;

public class generator2 extends supergenerator {

	public static void main(String[] args) throws ClassNotFoundException,
			IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {

		// вводим нужный язык Библии
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Пожалуйста, введите один язык Библии из 10 (ru,en,de,es,fr,pt,zh,gr,it,tr) и нажмите <enter>:\n");

			String lang = in.nextLine();

			if (!lang.contains("all")) {

				generation(lang);

			} else {

				int size = langs50.length;
				for (int i = 0; i < size; i++) {
					generation(langs50[i]);
				}

			}

		}

	}

}
