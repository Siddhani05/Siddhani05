/*DSA MINI PROJECT- Splitwise- Spllit Expense
 UCE2023435-Anushka Kondkar
 UCE2023439-Siddhani Magar
 UCE2023444-Mayuri Dandekar
 */


import java.util.*;

// Class to represent a User
class User {
    int userId;
    String name;
    String email;
    String mobileNumber;
    List<String> history; // Store each user's transaction history

    public User(int userId, String name, String email, String mobileNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.history = new ArrayList<>();
    }

    // Add a transaction to the user's history
    public void addToHistory(String transaction) {
        history.add(transaction);
    }

    // Display the user's transaction history
    public void displayHistory() {
        System.out.println("\nTransaction History for " + name + " (User ID: " + userId + "):");
        if (history.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (String transaction : history) {
                System.out.println(transaction);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("| %-5d | %-15s | %-25s | %-15s |", userId, name, email, mobileNumber);
    }
}

// Class to represent an Expense
class Expense {
    int payerId;
    double amount;
    List<Integer> participants;
    String type; // "equal", "exact", or "percent"
    Map<Integer, Double> splitDetails;

    public Expense(int payerId, double amount, List<Integer> participants, String type,
            Map<Integer, Double> splitDetails) {
        this.payerId = payerId;
        this.amount = amount;
        this.participants = participants;
        this.type = type;
        this.splitDetails = splitDetails;
    }
}

// Splitwise Application Class
class SplitwiseApp {
    Map<Integer, User> users = new HashMap<>();
    List<Expense> expenses = new ArrayList<>();
    Map<Integer, Map<Integer, Double>> balances = new HashMap<>();

    // Method to add a user
    public void addUser(int userId, String name, String email, String mobileNumber) {
        User user = new User(userId, name, email, mobileNumber);
        users.put(userId, user);
        balances.put(userId, new HashMap<>());
    }

    // Method to add an expense
    public void addExpense(int payerId, double amount, List<Integer> participants, String type,
            Map<Integer, Double> splitDetails) {
        Expense expense = new Expense(payerId, amount, participants, type, splitDetails);
        expenses.add(expense);
        updateBalances(expense);
        logTransactionHistory(expense);
    }

    // Method to update balances after adding an expense
    private void updateBalances(Expense expense) {
        double totalAmount = expense.amount;

        if (expense.type.equals("equal")) {
            double splitAmount = Math.round((totalAmount / expense.participants.size()) * 100.0) / 100.0;
            distributeAmount(expense.payerId, expense.participants, splitAmount);
        } else if (expense.type.equals("exact")) {
            double sum = 0;
            for (double share : expense.splitDetails.values()) {
                sum += share;
            }
            if (sum == totalAmount) {
                for (Map.Entry<Integer, Double> entry : expense.splitDetails.entrySet()) {
                    int userId = entry.getKey();
                    double amount = entry.getValue();
                    updateBalance(expense.payerId, userId, amount);
                }
            } else {
                System.out.println("Error: Exact shares do not match total amount!");
            }
        } else if (expense.type.equals("percent")) {
            double sumPercent = 0;
            for (double percent : expense.splitDetails.values()) {
                sumPercent += percent;
            }
            if (sumPercent == 100) {
                for (Map.Entry<Integer, Double> entry : expense.splitDetails.entrySet()) {
                    int userId = entry.getKey();
                    double amount = Math.round((totalAmount * entry.getValue() / 100) * 100.0) / 100.0;
                    updateBalance(expense.payerId, userId, amount);
                }
            } else {
                System.out.println("Error: Percent shares do not sum up to 100%!");
            }
        }
    }

    private void distributeAmount(int payerId, List<Integer> participants, double amountPerPerson) {
        for (int userId : participants) {
            if (userId != payerId) {
                updateBalance(payerId, userId, amountPerPerson);
            }
        }
    }

    private void updateBalance(int payerId, int userId, double amount) {
        balances.get(userId).put(payerId, balances.get(userId).getOrDefault(payerId, 0.0) + amount);
        balances.get(payerId).put(userId, balances.get(payerId).getOrDefault(userId, 0.0) - amount);
    }

    // // Method to log each transaction to user history
    // private void logTransactionHistory(Expense expense) {
    // String payer = users.get(expense.payerId).name;
    // String expenseDescription = String.format("%s paid %.2f", payer,
    // expense.amount);

    // for (int participant : expense.participants) {
    // if (expense.splitDetails.containsKey(participant)) {
    // expenseDescription += String.format(", %s owes %.2f",
    // users.get(participant).name,
    // expense.splitDetails.get(participant));
    // }
    // }

    // // Add transaction to payer's history
    // users.get(expense.payerId).addToHistory(expenseDescription);

    // // Add transaction to participants' history
    // for (int participant : expense.participants) {
    // if (participant != expense.payerId) {
    // users.get(participant).addToHistory(expenseDescription);
    // }
    // }
    // }

    private void logTransactionHistory(Expense expense) {
        String payer = users.get(expense.payerId).name;
        String expenseDescription = String.format("%s paid %.2f", payer, expense.amount);

        // Log transaction for the payer
        users.get(expense.payerId).addToHistory(expenseDescription);

        // Add transaction details for participants
        for (int participant : expense.participants) {
            if (participant != expense.payerId) {
                if (expense.type.equals("equal")) {
                    double amountOwed = Math.round((expense.amount / expense.participants.size()) * 100.0) / 100.0;
                    String participantDescription = String.format("%s owes %.2f to %s", users.get(participant).name,
                            amountOwed, payer);
                    users.get(participant).addToHistory(participantDescription);
                } else if (expense.splitDetails.containsKey(participant)) {
                    double amountOwed = expense.splitDetails.get(participant);
                    String participantDescription = String.format("%s owes %.2f to %s", users.get(participant).name,
                            amountOwed, payer);
                    users.get(participant).addToHistory(participantDescription);
                }
            }
        }
    }

    // Method to show balances for all users
    public void showAllBalances() {

        System.out.println(String.format(
                "| %-7s | %-12s | %-15s | %-15s | %-12s | %-7s |",
                "User ID", "Name", "Email", "Mobile Number", "Balance With", "Amount"));

        System.out.println(
                "------------------------------------------------------------------------------------------------------");

        for (Map.Entry<Integer, Map<Integer, Double>> entry : balances.entrySet()) {
            int userId = entry.getKey();
            Map<Integer, Double> userBalances = entry.getValue();
            for (Map.Entry<Integer, Double> balanceEntry : userBalances.entrySet()) {
                if (balanceEntry.getValue() != 0) {
                    System.out.println(String.format("| %-7d | %-15s | %-25s | %-15s | %-12d | %-7.2f |",
                            userId, users.get(userId).name, users.get(userId).email, users.get(userId).mobileNumber,
                            balanceEntry.getKey(), balanceEntry.getValue()));
                }
            }
        }
    }

    // Method to display the transaction history of a specific user
    public void showUserHistory(int userId) {
        if (users.containsKey(userId)) {
            users.get(userId).displayHistory();

        } else {
            System.out.println("User ID not found.");
        }
    }

    // Utility methods for user input
    public static String getInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static double getDoubleInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextDouble();
    }

    public static int getIntInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextInt();
    }

    public void removeUser(int userId) {
        if (!users.containsKey(userId)) {
            System.out.println("User not found.");
            return;
        }

        User user = users.get(userId);
        System.out.println("Removing user: " + user.name);

        // Display outstanding balances before removing
        Map<Integer, Double> userBalances = balances.get(userId);
        if (!userBalances.isEmpty()) {
            System.out.println("Outstanding balances for " + user.name + ":");
            for (Map.Entry<Integer, Double> balance : userBalances.entrySet()) {
                if (balance.getValue() != 0) {
                    System.out.println("With User ID " + balance.getKey() + ": " + balance.getValue());
                }
            }
        }
        user.name += " (Inactive)";
        System.out.println("User " + user.name + " marked as inactive.");
    }

}

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        SplitwiseApp app = new SplitwiseApp();

        // Adding users
        int userCount = app.getIntInput("Enter the number of users: ");
        for (int i = 0; i < userCount; i++) {
            System.out.println("Enter details for User " + (i + 1));
            int userId = app.getIntInput("Enter User ID: ");
            String name = app.getInput("Enter Name: ");
            String email = app.getInput("Enter Email: ");
            String mobileNumber = app.getInput("Enter Mobile Number: ");
            app.addUser(userId, name, email, mobileNumber);
        }

        // Adding expenses
        while (true) {
            System.out.println("\nAdd an Expense");
            int payerId = app.getIntInput("Enter Payer User ID: ");
            double amount = app.getDoubleInput("Enter Total Amount: ");
            List<Integer> participants = new ArrayList<>();
            int participantCount = app.getIntInput("Enter the number of participants: ");
            for (int i = 0; i < participantCount; i++) {
                participants.add(app.getIntInput("Enter Participant User ID: "));
            }
            String type = app.getInput("Enter Expense Type (equal/exact/percent): ").toLowerCase();
            Map<Integer, Double> splitDetails = new HashMap<>();
            if (type.equals("exact") || type.equals("percent")) {
                for (int userId : participants) {
                    double share = app.getDoubleInput("Enter share for User ID " + userId + ": ");
                    splitDetails.put(userId, share);
                }
            }
            app.addExpense(payerId, amount, participants, type, splitDetails);

            String anotherExpense = app.getInput("Do you want to add another expense? (yes/no): ").toLowerCase();
            if (!anotherExpense.equals("yes")) {
                break;
            }
        }

        String deleteUserResponse = app.getInput("Do you want to delete any user? (yes/no): ").toLowerCase();
        if (deleteUserResponse.equals("yes")) {
            int userIdToRemove = app.getIntInput("Enter the User ID to remove: ");
            app.removeUser(userIdToRemove);
        } else {
            System.out.println("No user removed.");
        }

        // Main menu for displaying options to view balances or history
        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Show All Balances");
            System.out.println("2. Show User Transaction History");
            System.out.println("3. Exit");

            int choice = app.getIntInput("Enter your choice: ");
            switch (choice) {
                case 1:
                    app.showAllBalances();
                    break;
                case 2:
                    int userId = app.getIntInput("Enter User ID to view history: ");
                    app.showUserHistory(userId);
                    break;
                case 3:
                    System.out.println("Exiting the application. Goodbye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}


/*Enter the number of users: 5
Enter details for User 1
Enter User ID: 1
Enter Name: Sid
Enter Email: sid@gmail.com
Enter Mobile Number: 8989898989 
Enter details for User 2
Enter User ID: 2
Enter Name: Anu
Enter Email: anu@gmail.com
Enter Mobile Number: 6789054321
Enter details for User 3
Enter User ID: 3
Enter Name: May
Enter Email: may@gmail.com
Enter Mobile Number: 9999988888
Enter details for User 4
Enter User ID: 4
Enter Name: Sam
Enter Email: sam@gmail.com
Enter Mobile Number: 7890789090
Enter details for User 5
Enter User ID: 5
Enter Name: sai
Enter Email: sai@gmail.com
Enter Mobile Number: 8888890909

Add an Expense
Enter Payer User ID: 1
Enter Total Amount: 1000
Enter the number of participants: 5
Enter Participant User ID: 1
Enter Participant User ID: 2
Enter Participant User ID: 3
Enter Participant User ID: 4
Enter Participant User ID: 5
Enter Expense Type (equal/exact/percent): equal
Do you want to add another expense? (yes/no): yes

Add an Expense
Enter Payer User ID: 1
Enter Total Amount: 2000
Enter the number of participants: 5
Enter Participant User ID: 1
Enter Participant User ID: 2
Enter Participant User ID: 3
Enter Participant User ID: 4
Enter Participant User ID: 5
Enter Expense Type (equal/exact/percent): exact
Enter share for User ID 1: 500
Enter share for User ID 2: 500
Enter share for User ID 3: 200
Enter share for User ID 4: 300
Enter share for User ID 5: 500
Do you want to add another expense? (yes/no): yes

Add an Expense
Enter Payer User ID: 1
Enter Total Amount: 500
Enter the number of participants: 5
Enter Participant User ID: 1
Enter Participant User ID: 2
Enter Participant User ID: 3
Enter Participant User ID: 4
Enter Participant User ID: 5
Enter Expense Type (equal/exact/percent): percent
Enter share for User ID 1: 20
Enter share for User ID 2: 20
Enter share for User ID 3: 20
Enter share for User ID 4: 20
Enter share for User ID 5: 20
Do you want to add another expense? (yes/no): no
Do you want to delete any user? (yes/no): no
No user removed.

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 1
| User ID | Name         | Email           | Mobile Number   | Balance With |     Amount  |
------------------------------------------------------------------------------------------------------
| 1       | Sid             | sid@gmail.com             | 8989898989      | 2     | -800.00 |
| 1       | Sid             | sid@gmail.com             | 8989898989      | 3     | -500.00 |
| 1       | Sid             | sid@gmail.com             | 8989898989      | 4     | -600.00 |
| 1       | Sid             | sid@gmail.com             | 8989898989      | 5     | -800.00 |
| 2       | Anu             | anu@gmail.com             | 6789054321      | 1     | 800.00  |
| 3       | May             | may@gmail.com             | 9999988888      | 1     | 500.00  |
| 4       | Sam             | sam@gmail.com             | 7890789090      | 1     | 600.00  |
| 5       | sai             | sai@gmail.com             | 8888890909      | 1     | 800.00  |

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 2
Enter User ID to view history: 1

Transaction History for Sid (User ID: 1):
Sid paid 1000.00
Sid paid 2000.00
Sid paid 500.00

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 2
Enter User ID to view history: 2

Transaction History for Anu (User ID: 2):
Anu owes 200.00 to Sid
Anu owes 500.00 to Sid
Anu owes 20.00 to Sid

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 2
Enter User ID to view history: 3

Transaction History for May (User ID: 3):
May owes 200.00 to Sid
May owes 200.00 to Sid
May owes 20.00 to Sid

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 2
Enter User ID to view history: 5

Transaction History for sai (User ID: 5):
sai owes 200.00 to Sid
sai owes 500.00 to Sid
sai owes 20.00 to Sid

Select an option:
1. Show All Balances
2. Show User Transaction History
3. Exit
Enter your choice: 3
Exiting the application. Goodbye!
PS C:\Users\siddh\OneDrive\Desktop\dsaminiproject>  */