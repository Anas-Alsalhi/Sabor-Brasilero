package com.restaurant;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class RestaurantApp {

    // A list of waiter names that will be randomly assigned to tables.
    private static final List<String> WAITER_NAMES = List.of("Sophia", "Liam", "Olivia", "Noah", "Emma");

    // The total number of languages supported by the application.
    private static final int LANGUAGE_COUNT = 10;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // The Menu object represents the restaurant's menu, containing all available dishes.
            Menu menu = new Menu();

            // The OrderHistory object keeps track of all orders placed by customers.
            OrderHistory orderHistory = new OrderHistory();

            // This flag controls whether the application should keep running or exit.
            boolean exit = false;

            // Step 1: Display the language selection menu to the user.
            printLanguageSelectionMenu();

            // Step 2: Get the user's choice of language and set the corresponding locale (language and region).
            int languageChoice = getValidLanguageChoice(scanner);
            Locale locale = getLocaleForLanguageChoice(languageChoice);

            // Step 3: Load the appropriate resource bundle (translations) for the selected language.
            ResourceBundle messages = loadResourceBundle(locale);

            // Step 4: Display a welcome message in the selected language.
            printWelcomeMessage(messages);

            // Step 5: Main loop of the application. This keeps running until the user chooses to exit.
            while (!exit) {
                try {
                    // Display the main menu options to the user.
                    printMainMenu(messages);

                    // Get the user's choice from the main menu.
                    int choice = getValidMenuChoice(scanner, messages);

                    // Handle the user's choice using a switch statement.
                    switch (choice) {
                        case 1 -> menu.displayMenuByCategory(messages); // Show the menu grouped by dish type.
                        case 2 -> menu.displayDailySpecials(3, messages); // Show today's special dishes with a limit of 3.
                        case 3 -> processOrder(menu, scanner, orderHistory, messages, locale); // Allow the user to place an order.
                        case 4 -> orderHistory.displayHistory(); // Show the history of all previous orders.
                        case 5 -> saveOrderHistory(scanner, orderHistory, messages); // Save the order history to a file.
                        case 6 -> loadOrderHistory(scanner, orderHistory, messages); // Load order history from a file.
                        case 7 -> bookEvent(scanner, messages); // Allow the user to book an event.
                        case 8 -> recommendDishes(orderHistory, menu, messages); // Show AI-powered dish recommendations.
                        case 9 -> saveOrderHistoryAsText(scanner, orderHistory, messages);
                        case 10 -> loadOrderHistoryFromText(scanner, orderHistory, messages);
                        case 11 -> {
                            // Exit the application.
                            System.out.println("\n" + messages.getString("thank_you"));
                            exit = true;
                        }
                        default -> System.out.println(messages.getString("invalid_choice")); // Handle invalid menu choices.
                    }
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred: " + e.getMessage());
                }
            }

            // Ensure messages is not null before using it
            if (messages != null) {
                System.out.println(messages.getString("goodbye"));
            } else {
                System.err.println("Error: Resource bundle 'messages' is not initialized. Goodbye message cannot be displayed.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize the application: " + e.getMessage());
        }
    }

    // Displays the language selection menu to the user.
    private static void printLanguageSelectionMenu() {
        System.out.println("============================================");
        System.out.println("Select a language:");
        // List of supported languages. 
        System.out.println("1. English");
        System.out.println("2. Portuguese");
        System.out.println("3. French");
        System.out.println("4. Italian");
        System.out.println("5. Spanish");
        System.out.println("6. German");
        System.out.println("7. Chinese");
        System.out.println("8. Russian");
        System.out.println("9. Norwegian");
        System.out.println("10. Japanese");
        System.out.println("============================================");
    }

    // Prompts the user to select a language and ensures the input is valid.
    private static int getValidLanguageChoice(Scanner scanner) {
        int languageChoice = -1;
        while (languageChoice < 1 || languageChoice > LANGUAGE_COUNT) {
            System.out.print("Please enter a number between 1 and " + LANGUAGE_COUNT + ": ");
            if (scanner.hasNextInt()) {
                languageChoice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character.
                if (languageChoice < 1 || languageChoice > LANGUAGE_COUNT) {
                    System.out.println("Invalid choice. Try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume the invalid input.
            }
        }
        return languageChoice;
    }

    // Maps the user's language choice to a Locale object, which represents the language and region.
    // This helps in loading the correct translations for the application.
    private static Locale getLocaleForLanguageChoice(int languageChoice) {
        Map<Integer, Locale> localeMap = Map.of(
            1, Locale.ENGLISH,
            2, Locale.of("pt", "PT"), // Portuguese
            3, Locale.of("fr", "FR"), // French
            4, Locale.of("it", "IT"), // Italian
            5, Locale.of("es", "ES"), // Spanish
            6, Locale.GERMAN,
            7, Locale.CHINA, // Simplified Chinese
            8, Locale.of("ru", "RU"), // Russian
            9, Locale.of("no", "NO"), // Norwegian
            10, Locale.JAPAN // Japanese
        );
        return localeMap.getOrDefault(languageChoice, Locale.ENGLISH); // Default to English if the choice is invalid.
    }

    // Loads the appropriate resource bundle (translations) for the selected language.
    // If the resource bundle is missing, it falls back to English.
    private static ResourceBundle loadResourceBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle("com.restaurant.messages", locale);
        } catch (MissingResourceException e) {
            System.err.println("Missing resource bundle for locale: " + locale + ". Falling back to English.");
            return ResourceBundle.getBundle("com.restaurant.messages", Locale.ENGLISH);
        }
    }

    // Displays a welcome message in the selected language.
    // The message includes the restaurant's name and tagline.
    private static void printWelcomeMessage(ResourceBundle messages) {
        System.out.println();
        System.out.println("============================================");
        // Replace the restaurant name in the welcome message with the actual name.
        System.out.println(messages.getString("welcome").replace("Sabor Brasileiro", "Galway To São Paulo Restaurant"));
        System.out.println(messages.getString("tagline")); // Display the restaurant's tagline.
        System.out.println("============================================\n");
    }

    // Displays the main menu options to the user.
    // Each option corresponds to a specific feature of the application.
    private static void printMainMenu(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("choose_option"));
        System.out.println("1. " + messages.getString("view_menu")); 
        System.out.println("2. " + messages.getString("view_specials"));
        System.out.println("3. " + messages.getString("place_order")); 
        System.out.println("4. " + messages.getString("view_history")); 
        System.out.println("5. " + messages.getString("save_history")); 
        System.out.println("6. " + messages.getString("load_history")); 
        System.out.println("7. " + messages.getString("book_event")); 
        System.out.println("8. " + messages.getString("ai_recommendations_header")); 
        System.out.println("9. " + messages.getString("save_as_text"));
        System.out.println("10. " + messages.getString("load_as_text"));
        System.out.println("11. " + messages.getString("exit")); 
        System.out.print(messages.getString("enter_choice"));
    }

    // Gets a valid menu choice from the user.
    // Ensures that the input is a valid number corresponding to a menu option.
    private static int getValidMenuChoice(Scanner scanner, ResourceBundle messages) {
        int choice = -1;
        while (choice < 1 || choice > 11) { // Ensure valid menu choice range
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character.
            } else {
                System.out.println(messages.getString("invalid_input")); // Display an error message for invalid input.
                scanner.nextLine(); // Consume the invalid input.
            }
        }
        return choice;
    }

    // Method to save the order history to a file.
    // Prompts the user for a file name and saves the history in that file.
    private static void saveOrderHistory(Scanner scanner, OrderHistory orderHistory, ResourceBundle messages) {
        System.out.print(messages.getString("enter_file_save"));
        String fileName = scanner.nextLine();
        Path filePath = Paths.get(fileName);
        orderHistory.saveToFile(filePath);
    }

    // Method to load the order history from a file.
    // Prompts the user for a file name and loads the history from that file.
    private static void loadOrderHistory(Scanner scanner, OrderHistory orderHistory, ResourceBundle messages) {
        System.out.print(messages.getString("enter_file_load"));
        String fileName = scanner.nextLine();
        Path filePath = Paths.get(fileName);
        orderHistory.loadFromFile(filePath);
    }

    // Method to process a new order.
    // This includes selecting dishes, calculating discounts, and finalizing the order.
    private static void processOrder(Menu menu, Scanner scanner, OrderHistory orderHistory, ResourceBundle messages, Locale locale) {
        if (messages == null) {
            System.err.println("Error: Resource bundle 'messages' is not initialized. Exiting application.");
            return;
        }
        Order order = null;
        double discount = 0; // Ensure this is the only declaration of 'discount'
        try {
            List<Table> tables = List.of(
                    new Table(1, 2),
                    new Table(2, 4),
                    new Table(3, 6),
                    new Table(4, 8)
            );
            Random random = new Random();
            Table table = tables.get(random.nextInt(tables.size()));
            int seatedCustomers = random.nextInt(table.getCapacity()) + 1;
            String waiterName = WAITER_NAMES.get(random.nextInt(WAITER_NAMES.size()));
            int waiterId = random.nextInt(1000);
            Waiter waiter = new Waiter(waiterName, waiterId);

            order = new Order(table, waiter, messages);
            order.printSummary();
            List<Dish> allDishes = menu.getAllDishes();

            System.out.println("\n" + "=".repeat(50));
            System.out.printf("%25s%n", messages.getString("menu"));
            System.out.println("=".repeat(50));

            for (int i = 0; i < allDishes.size(); i++) {
                Dish dish = allDishes.get(i);
                System.out.printf("%-3d %-30s (€ %.2f)%n", i + 1, dish.name(), dish.price());
            }

            System.out.println("=".repeat(50));

            while (true) {
                System.out.print(messages.getString("enter_dish_number"));
                if (scanner.hasNextInt()) {
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice < 0) {
                        System.out.println(messages.getString("invalid_number"));
                        continue;
                    }
                    if (choice == 0) {
                        if (order.getDishes().isEmpty()) {
                            System.out.println(messages.getString("error") + ": " + messages.getString("no_dishes_added"));
                            continue; // Ensure at least one dish is added
                        }
                        break;
                    }
                    if (choice > 0 && choice <= allDishes.size()) {
                        Dish selectedDish = allDishes.get(choice - 1);
                        if (menu.isDishAvailable(selectedDish)) {
                            try {
                                order.addDish(selectedDish);
                                System.out.printf(messages.getString("added_dish_format"), selectedDish.name(), selectedDish.price());
                            } catch (InvalidOrderException e) {
                                System.out.println(messages.getString("error") + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println(messages.getString("dish_not_available"));
                        }
                    } else {
                        System.out.println(messages.getString("invalid_number"));
                    }
                } else {
                    System.out.println(messages.getString("invalid_input"));
                    scanner.nextLine();
                }
            }

            System.out.println("\n" + messages.getString("order_summary"));
            System.out.println(messages.getString("table_details"));
            System.out.println(String.format("  %s", String.format(messages.getString("capacity"), table.getCapacity())));
            System.out.println(String.format("  %s", String.format(messages.getString("seated_customers"), seatedCustomers)));
            System.out.println(String.format("  %s", String.format(messages.getString("waiter"), waiter.getName())));

            System.out.println("\n" + messages.getString("ordered_items"));
            order.printDetails();

            double total = order.getDishes().stream().mapToDouble(Dish::price).sum();
            System.out.println(String.format("\n%s", String.format(messages.getString("subtotal"), total)));

            while (true) {
                System.out.print(messages.getString("enter_discount").replace("(0-25)", "(0-25%)"));
                String discountInput = scanner.nextLine().trim();

                if (discountInput.isEmpty()) {
                    break;
                }

                try {
                    discount = Double.parseDouble(discountInput); // Use the existing 'discount' variable
                    if (discount >= 0 && discount <= 25) {
                        break;
                    } else {
                        System.out.println(messages.getString("invalid_discount_range"));
                    }
                } catch (NumberFormatException e) {
                    System.out.println(messages.getString("invalid_discount_format"));
                }
            }

            double discountedTotal = total - (total * (discount / 100));
            System.out.println(String.format("\n%s", String.format(messages.getString("total_after_discount"), discountedTotal)));

            order.setDiscountPercentage(discount);
            order.setFinalPrice(discountedTotal);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", locale);
            String formattedTimestamp = dateFormat.format(new Date());
            System.out.printf(messages.getString("order_timestamp").replace(" (dd/MM/yyyy HH:mm)", "") + "%n", formattedTimestamp);
            System.out.println(); // Add a blank line for better readability

            Map<String, Long> dishCounts = order.getDishes().stream()
                .collect(Collectors.groupingBy(Dish::name, Collectors.counting()));

            // Replace Thread.sleep with ScheduledExecutorService for non-blocking delays
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

            List<String> orderedDishNames = order.getDishes().stream()
                .map(Dish::name)
                .distinct()
                .collect(Collectors.toList());

            for (String dishName : orderedDishNames) {
                long count = dishCounts.get(dishName);

                for (int i = 1; i <= count; i++) {
                    int dishIndex = i;
                    scheduler.schedule(() -> {
                        try {
                            // Use the original casing of dish names from the menu
                            String originalDishName = dishName;
                            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            System.out.printf("[%s] --- Preparing dish: %s (%d of %d) --- %n", timestamp, originalDishName, dishIndex, count);

                            // Simulate preparation time
                            TimeUnit.MILLISECONDS.sleep(1000 + new Random().nextInt(2000));

                            timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            System.out.printf("[%s] >>> Dish prepared: %s (%d of %d)! %n", timestamp, originalDishName, dishIndex, count);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println(messages.getString("preparation_interrupted"));
                        }
                    }, 1000 + new Random().nextInt(2000), TimeUnit.MILLISECONDS);
                }
            }

            scheduler.shutdown();
            try {
                scheduler.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                System.err.println(messages.getString("preparation_interrupted") + ": " + e.getMessage());
            }

            System.out.println("\n" + messages.getString("all_dishes_prepared"));

            try (ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor()) {
                Future<String> processingTask = singleThreadExecutor.submit(() -> {
                    Thread.sleep(2000);
                    return messages.getString("order_processed");
                });

                System.out.println("\n" + processingTask.get());
            } catch (Exception e) {
                System.err.println(messages.getString("order_failed") + ": " + e.getMessage());
            }

            if (!orderHistory.getOrders().contains(order)) {
                orderHistory.addOrder(order);
                System.out.println("\n" + messages.getString("order_added_history"));
            }

            // Save order summary to a text file
            try {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmm").format(new Date());
                Path ordersDir = Paths.get("orders");
                if (!Files.exists(ordersDir)) {
                    Files.createDirectories(ordersDir);
                }
                Path orderFile = ordersDir.resolve("order_" + timestamp + ".txt");

                try (BufferedWriter writer = Files.newBufferedWriter(orderFile)) {
                    writer.write("Order Summary\n");
                    writer.write("=============\n");
                    writer.write(String.format("Table Number: %d\n", table.getTableNumber()));
                    writer.write(String.format("Waiter: %s\n", waiter.getName()));
                    writer.write(String.format("Seated Customers: %d\n", seatedCustomers));
                    writer.write("\nOrdered Items:\n");
                    for (Dish dish : order.getDishes()) {
                        writer.write(String.format("- %s (€%.2f)\n", dish.name(), dish.price()));
                    }
                    writer.write(String.format("\nSubtotal: €%.2f\n", total));
                    writer.write(String.format("Discount: %.2f%%\n", discount));
                    writer.write(String.format(messages.getString("total.after.discount") + "\n", discountedTotal));
                    writer.write(String.format("Order Timestamp: %s\n", formattedTimestamp));
                    writer.write("\nDish Preparation Logs:\n");
                    for (String dishName : orderedDishNames) {
                        long count = dishCounts.get(dishName);
                        for (int i = 1; i <= count; i++) {
                            writer.write(String.format("- %s (%d of %d) prepared\n", dishName, i, count));
                        }
                    }
                    writer.write("\n" + messages.getString("order_processed") + "\n");
                    writer.write(messages.getString("order_added_history") + "\n");
                }

                System.out.println(messages.getString("order_summary_saved") + ": " + orderFile.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to save order summary: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println(messages.getString("error_processing_order") + ": " + e.getMessage());
        }

        // Consolidate and display unique ordered dish descriptions
        // Build a map of unique dish names to descriptions (first occurrence only)
        Map<String, String> uniqueDescriptions = new LinkedHashMap<>();
        // Ensure order is not null before accessing its methods
        if (order != null) {
            order.getDishes().forEach(dish -> 
                uniqueDescriptions.putIfAbsent(dish.name(), dish.getDescription())
            );
        } else {
            System.err.println("Error: Order object is null. Cannot process dishes.");
        }

        // Format for display
        System.out.println("\nOrdered Dish Descriptions:");
        uniqueDescriptions.forEach((dishName, description) -> 
            System.out.println(" - " + dishName + ": " + description)
        );

        // Display order details (with quantities)
        System.out.println("\nOrder Details:");
        // Ensure order is not null before accessing its methods
        if (order != null) {
            Map<String, Long> dishQuantities = order.getDishes().stream()
                .collect(Collectors.groupingBy(Dish::name, Collectors.counting()));
            dishQuantities.forEach((dishName, quantity) ->
                System.out.println(" - " + dishName + " x" + quantity)
            );
        } else {
            System.err.println("Error: Order object is null. Cannot display order details.");
        }

        // Display vegetarian dishes
        Predicate<Dish> isVegetarian = Dish::isVegetarian;
        List<Dish> vegetarianDishes = menu.getAllDishes().stream()
            .filter(isVegetarian)
            .collect(Collectors.toList());

        System.out.println("\n" + String.format(messages.getString("vegetarian_dishes"), vegetarianDishes.size()));
        vegetarianDishes.forEach(dish -> 
            System.out.println(" - " + dish.name() + ": " + dish.getLocalizedDescription(messages))
        );

        // Display summary
        // Ensure order is not null before accessing its methods
        if (order != null) {
            int totalDishes = order.getDishes().size();
            // Removed unused assignment to avoid the compile error
            double finalPrice = order.getFinalPrice();

            System.out.println();
            System.out.printf(messages.getString("total_dishes") + "\n", totalDishes);
            System.out.printf(messages.getString("total_after_discount") + "\n", finalPrice);
        } else {
            System.err.println("Error: Order object is null. Cannot display summary.");
        }
    }

    // Method to book an event.
    // Collects event details such as name, date, time, and guest count.
    private static void bookEvent(Scanner scanner, ResourceBundle messages) {
        System.out.println("\n" + messages.getString("event_booking_header"));
        System.out.print(messages.getString("enter_event_name"));
        String eventName = scanner.nextLine();

        String eventDate;
        while (true) {
            System.out.print(messages.getString("enter_event_date"));
            eventDate = scanner.nextLine().trim();
            if (eventDate.isEmpty()) {
                System.out.println(messages.getString("invalid_event_date_format"));
                continue;
            }
            try {
                // Explicitly set the date format to DD/MM/YYYY
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.getDefault());
                LocalDate enteredDate = LocalDate.parse(eventDate, formatter);
                if (enteredDate.isAfter(LocalDate.now())) {
                    break;
                } else {
                    System.out.println(messages.getString("invalid_event_date_future"));
                }
            } catch (DateTimeParseException e) {
                System.out.println(messages.getString("invalid_event_date_format"));
            }
        }

        System.out.print(messages.getString("enter_event_time"));
        String eventTime;
        while (true) {
            eventTime = scanner.nextLine().trim();
            if (eventTime.isEmpty()) {
                System.out.println(messages.getString("invalid_event_time_format"));
                continue;
            }
            try {
                // Explicitly set the time format to HH:mm
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.getDefault());
                LocalTime.parse(eventTime, timeFormatter);
                break; // Valid time format
            } catch (DateTimeParseException e) {
                System.out.println(messages.getString("invalid_event_time_format"));
            }
        }

        System.out.print(messages.getString("enter_guest_count"));
        int guestCount = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\n" + messages.getString("event_booking_summary"));
        System.out.printf(messages.getString("event_name"), eventName);
        System.out.printf(messages.getString("event_date"), eventDate);
        System.out.printf(messages.getString("event_time"), eventTime);
        System.out.printf(messages.getString("guest_count"), guestCount);

        System.out.print("\n" + messages.getString("confirm_booking"));
        String confirmation = scanner.nextLine().trim().toLowerCase();

        String yesLocalized = messages.getString("yes").toLowerCase();
        if (confirmation.equals(yesLocalized)) {
            System.out.println(messages.getString("event_booking_success"));
        } else {
            System.out.println(messages.getString("event_booking_cancelled"));
        }
    }

    // Method to recommend dishes based on order history.
    // Uses AI-powered logic to suggest the most frequently ordered dishes.
    private static void recommendDishes(OrderHistory orderHistory, Menu menu, ResourceBundle messages) {
        System.out.println("\n" + messages.getString("ai_recommendations_header").replace("IA", "AI"));

        var dishFrequency = orderHistory.getOrders().stream()
            .flatMap(order -> order.getDishes().stream())
            .collect(Collectors.groupingBy(Dish::name, Collectors.counting()));

        List<Dish> recommendedDishes = dishFrequency.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(3)
            .map(entry -> menu.getAllDishes().stream()
                .filter(dish -> dish.name().equals(entry.getKey()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Dish not found")))
            .collect(Collectors.toList());

        if (recommendedDishes.isEmpty()) {
            System.out.println(messages.getString("no_recommendations"));
        } else {
            System.out.println(messages.getString("recommended_dishes"));
            recommendedDishes.forEach(dish ->
                System.out.printf(" - %-25s (€%.2f)%n", dish.name(), dish.price())
            );
        }
    }

    private static void saveOrderHistoryAsText(Scanner scanner, OrderHistory orderHistory, ResourceBundle messages) {
        System.out.print(messages.getString("enter_file_save"));
        String fileName = scanner.nextLine();
        Path filePath = Paths.get(fileName);
        orderHistory.saveAsText(filePath);
    }

    private static void loadOrderHistoryFromText(Scanner scanner, OrderHistory orderHistory, ResourceBundle messages) {
        System.out.print(messages.getString("enter_file_load"));
        String fileName = scanner.nextLine();
        Path filePath = Paths.get(fileName);
        orderHistory.loadFromText(filePath);
    }
}
