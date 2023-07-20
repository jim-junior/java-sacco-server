import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public Socket socket = null;
    public BufferedReader input = null;
    public PrintWriter output = null;
    public boolean loggedIn = false;
    public BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));


    public Client() {
        try {
            socket = new Socket("localhost", 8080);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            login();
            if (loggedIn) {
                displayMenu();
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to server");
            System.out.println(e);
        }
    }


    public static void main(String[] args) {
        Client clientApp = new Client();
    }


    private void login() throws IOException {
        
        while(!loggedIn) {
            
            // Prompt for username and password
            System.out.print("Enter Command:");
            System.out.println("\n");
            String command = userInputReader.readLine();  
            
            
            // Send the login command with username and password
            output.println(command);
            
            // Receive the login response from the server
            String response = input.readLine();
            System.out.println("Server response: " + response);

            if(response.equals("Login successful")){
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




    public static void _main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            
            
            
    
            
            
            
            // Continue with the rest of the secure menu items if login is successful
            
            // Close the connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
}
