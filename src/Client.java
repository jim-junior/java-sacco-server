import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public Socket socket = null;
    public BufferedReader input = null;
    public PrintWriter output = null;
    public boolean loggedIn = false;
    public BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
    public boolean active = true;

    public Client() {
        try {
            socket = new Socket("localhost", 8080);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            login();
            if (loggedIn) {
                start();
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to server");
            System.out.println(e);
        }
    }

    public void start() {
        try {
            while (active) {
                displayMenu();
                String command = getUserInput();
                if (command.equals("logout")) {
                    output.println("logout");
                    break;
                }
                String[] commandParts = command.split(" ");
                String action = commandParts[0];
                if (action.equals("deposit")) {
                    output.println(command);
                    String res = input.readLine();
                    System.out.println(res);
                } else if (action.equals("loanrequeststatus")) {
                    output.println(command);
                    loanrequeststatus();
                }

            }
        } catch (Exception e) {
            System.out.println("An Error Occured");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Client clientApp = new Client();
    }

    private void login() throws IOException {

        while (!loggedIn) {

            // Prompt for username and password
            System.out.print("Enter Command:");
            System.out.println("\n");
            String command = userInputReader.readLine();

            if (!command.startsWith("login")) {
                System.out.println("Please login first");
                continue;
            }

            // Send the login command with username and password
            output.println(command);

            // Receive the login response from the server
            String response = input.readLine();
            System.out.println("Server response: " + response);

            if (response.equals("Login successful")) {
                loggedIn = true;
                break;
            }

            // If login unsuccessful, request member number and phone number
            if (response.equals("Please provide member number and phone number")) {
                // Prompt for member number and phone number
                System.out.print("Enter member number: ");
                String memberNumber = userInputReader.readLine();
                System.out.print("Enter phone number: ");
                String phoneNumber = userInputReader.readLine();

                // Send the member number and phone number to the server
                output.println(memberNumber + " " + phoneNumber);

                // Receive the response from the server
                response = input.readLine();
                System.out.println("Server response: " + response);

                // If the response contains a reference number, save it for future use
                if (response.startsWith("Partial password:")) {
                    String referenceNumber = response.substring(18);
                    loggedIn = true;
                    break;
                }
            }
        }
    }

    private static void displayMenu() {
        System.out.println("----- Sacco System Menu -----");
        System.out.println("1. Deposit");
        System.out.println("2. Check Statement");
        System.out.println("3. Request Loan");
        System.out.println("4. Check Loan Request Status");
        System.out.println("5. Logout");
        System.out.print("Enter your choice: ");
    }

    private static String getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }

    public void loanrequeststatus() {
        try {

            String serverResponse;
            while ((serverResponse = input.readLine()) != null) {
                System.out.println("Server says: " + serverResponse);

                if (serverResponse.startsWith("please enter the command")) {
                    String command = userInputReader.readLine();
                    output.println(command);
                } else if (serverResponse.startsWith("Do you want to accept or reject the loan?")) {
                    String decision = userInputReader.readLine();
                    output.println(decision);
                } else if (serverResponse.startsWith("Done:")) {
                    System.out.println("Done");
                    break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkLoanStatement() {
        try {

            String serverResponse;
            while ((serverResponse = input.readLine()) != null) {
                if (serverResponse.startsWith("Done:")) {
                    System.out.println("Done");
                    break;
                } else {
                    System.out.println(serverResponse);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
