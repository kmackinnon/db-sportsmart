package com.company;

import static com.company.util.Util.prettyPrintResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.company.option.*;
import com.company.util.Context;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Context userContext = new Context();

        Option[] options = {
            new Login(userContext),
            new Search(),
            new Clearance(),
            new TopSellingItems(),
            new AddToCart(userContext),
            new ConvertToPurchase(userContext)
        };

        while (true) { // the loops goes on until the user chooses to quit
            System.out.format("%nPlease select an option:%n");

            int i;
            for (i = 0; i < options.length; i++) {
                System.out.format("%d) %s%n", i + 1, options[i].getName());
            }

            System.out.format("%d) Quit%n", i + 1);

            String input = bufferedReader.readLine();

            int selection = 0;

            try {
                selection = Integer.parseInt(input.trim());

                if (selection == options.length + 1) {
                    System.out.println("Later skater");
                    return;
                } else if (selection < 1 || selection > options.length) {
                    System.out.format("There is no option %d, please enter a valid option%n", selection);
                } else {
                	launchSubMenu(options[selection - 1]);	
                }
            } catch (NumberFormatException numberFormatException) {
                System.out.println("Please enter a valid number, let's try again!");
            }
        }
    }

    private static void launchSubMenu(Option option) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        if (option.hasSubOptions()) {
        	System.out.println("To continue with your choice, provide data for the following fields");
        	// For each submenu option get string input and then sanitize and add to subOptionValues
            for (String subOptionName : option.getSubOptionNames()) {
                System.out.format("%s: ", subOptionName);
                String input = in.readLine();
                option.setSubOptionValue(subOptionName, input);
            }
        }
        
        System.out.println();

        // Once all submenus have been entered, run execute, catching any runtime execution errors due to invalid input
        try {
            Option.Result result = option.execute();

            if (result.message != null) {
                System.out.println(result.message);
            }

            if (result.results != null) {
                System.out.println();
                prettyPrintResults(result.results);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
