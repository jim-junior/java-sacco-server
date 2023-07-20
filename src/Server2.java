import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Server2 {
    private static final String DB_HOST = "jdbc:mysql://localhost:3306/sacco";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "PaulAkol@2244";
    private Connection con = null;

    private static Map<String, String> partialPasswords = new HashMap<>();

    public static void main(String[] args) {
        Server2 server = new Server2();
        server.start();
    }

    public void createConnection() {
        try {
            // Class.forName("com.mysql.cj.jdbc");
            con = DriverManager.getConnection(DB_HOST, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.out.println("Database connection failed");
            System.out.println(e);

        }

    }

    public void start() {
        try {
            createConnection();
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                System.out.println("Server started and listening on port 8080...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected:" + socket);

                    // Start a new thread to handle the client request
                    Thread thread = new Thread(() -> handleClientRequest(socket));
                    thread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(Socket socket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String request = input.readLine();

                if (request.equals("logout")) {
                    socket.close();
                    break;
                }

                // Process the request
                String response = processRequest(request, output, input);

                // Send the response back to the client
                output.println(response);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processRequest(String request, PrintWriter output, BufferedReader input) {
        String[] commandParts = request.split(" ");
        String command = commandParts[0].toLowerCase();

        switch (command) {
            case "login":
                return processLogin(commandParts, output, input);
            case "deposit":
                return deposit(commandParts);
            case "checkstatement":
                // TODO: Implement checkstatement command
                break;
            case "requestloan":
                // TODO: Implement requestloan command
                break;
            case "loanrequeststatus":
                // TODO: Implement loanrequeststatus command
                break;
            case "accept":
                // TODO: Implement accept command
                break;
            case "reject":
                // TODO: Implement reject command
                break;
            default:
                return "Invalid command";
        }

        return "Response to the client's request";
    }

    private String processLogin(String[] commandParts, PrintWriter output, BufferedReader input) {
        if (commandParts.length != 3) {
            return "Invalid login command";
        }

        String username = commandParts[1];
        String password = commandParts[2];

        try {
            String query = "SELECT * FROM member WHERE username = ? and password = ?";
            PreparedStatement selectStmt = con.prepareStatement(query);
            selectStmt.setString(1, username);
            selectStmt.setString(2, password);

            ResultSet result = selectStmt.executeQuery();
            if (result.next()) {
                return "Login successful";
            } else {
                output.println("Please provide member number and phone number");
                String response = input.readLine();
                String[] responsParts = response.split(" ");
                System.out.println(responsParts);
                String memberNumber = responsParts[0];
                String phoneNumber = responsParts[1];
                System.out.println(phoneNumber);
                System.out.println(memberNumber);
                String[] memberInfoStrings = { memberNumber, phoneNumber };

                String memberInfoResult = processMemberInfo(memberInfoStrings);

                return memberInfoResult;
            }
        } catch (Exception e) {

            System.out.println(e);
            return "Error on Server Occured";
        }

    }

    private String processMemberInfo(String[] commandParts) {
        if (commandParts.length != 2) {
            return "Invalid member info command";
        }

        String memberNumber = commandParts[0];
        String phoneNumber = commandParts[1];

        try {
            String query = "SELECT * FROM member WHERE member_number = ? and phone_number = ?";
            PreparedStatement selectStmt = con.prepareStatement(query);
            selectStmt.setString(1, memberNumber);
            selectStmt.setString(2, phoneNumber);
            ResultSet result = selectStmt.executeQuery();
            if (result.next()) {
                String partialPassword = generatePartialPassword(memberNumber, phoneNumber);
                return "Partial password: " + partialPassword;
            } else {
                return "Invalid member number or phone number. Return next Day";
            }

        } catch (Exception e) {
            return "An Error occured";
        }

    }

    private String generatePartialPassword(String memberNumber, String phoneNumber) {
        String partialPassword = "PARTIAL" + memberNumber.substring(0, 3) + phoneNumber.substring(0, 3);
        partialPasswords.put(memberNumber, partialPassword);
        return partialPassword;
    }

    public String deposit(String[] commandArgs) {
        try {
            String receiptId = commandArgs[1];
            Integer amount = Integer.parseInt(commandArgs[2]);
            String date = commandArgs[3];

            String query = "SELECT * FROM contributions WHERE receipt_number = ? and amount = ?";

            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, receiptId);
            preparedStatement.setInt(2, amount);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return "Deposit Successfull";
            }
            return "No Reciept Found. Please try again later after new information is uploaded.";

        } catch (Exception e) {
            return "An Error Occured";
        }
    }
}
