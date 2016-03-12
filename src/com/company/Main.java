package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) { // the loops goes on until the user chooses to quit
            System.out.print("\nPlease select an option:" +
                    "\n1.Login\n2.Add Item to Cart\n3.View Items in Cart\n4.Clear Cart\n5.Make Purchase\n6.Quit\n->");

            String input = bufferedReader.readLine();

            int option = 0;

            try {
                option = Integer.parseInt(input.trim());
                System.out.println("You have selected the option " + option + ".");
            } catch (NumberFormatException numberFormatException) {
                System.out.println("Please enter a valid number, let's try again!\n");
            }

            if (option == 6) {
                break;
            }
        }
    }
}
