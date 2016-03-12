package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Please select an option:" +
                    "\n1.Option1\n2.Option2\n3.Option3\n4.Option4\n5.Option5\n6.Quit\n->");

            String input = bufferedReader.readLine();
            int option = Integer.parseInt(input.trim());
            System.out.println("You have selected the option " + option + ".");

            if (option == 6) {
                break;
            }
        }
    }
}
