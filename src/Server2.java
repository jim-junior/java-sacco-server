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
    /* private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String MEMBER_NUMBER = "123456";
    private static final String PHONE_NUMBER = "987654321";
    private static final String partialPassword = "2468";  */
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
            //Class.forName("com.mysql.cj.jdbc");
            con = DriverManager.getConnection(DB_HOST, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.out.println("Database connection failed");            
            System.out.println(e);

        }
        
    }


    

    private void insertData() {
	        // Insert data into member table
	        String insertMemberQuery = "INSERT INTO member (username, password, member_number, phone_number) VALUES (?, ?, ?, ?)";
	        try (PreparedStatement insertMemberStmt = con.prepareStatement(insertMemberQuery)) {
	            insertMemberStmt.setString(1, "paul");
	            insertMemberStmt.setString(2, "seen");
	            insertMemberStmt.setString(3, "M001");
	            insertMemberStmt.setString(4, "1234567890");
	            insertMemberStmt.executeUpdate();
	        } catch (Exception e) {
                System.out.println("Failed to execute statement");
                System.out.println(e);

            }

	        /*
	        // Insert data into deposit table
	        String insertDepositQuery = "INSERT INTO deposit (member_id, amount, date_deposited, receipt_number) VALUES (?, ?, ?, ?)";
	        try (PreparedStatement insertDepositStmt = connection.prepareStatement(insertDepositQuery)) {
	            insertDepositStmt.setInt(1, 1); // Assuming the member ID is 1
	            insertDepositStmt.setBigDecimal(2, BigDecimal.valueOf(1000.00));
	            insertDepositStmt.setDate(3, Date.valueOf(LocalDate.now()));
	            insertDepositStmt.setString(4, "123456");
	            insertDepositStmt.executeUpdate();
	        }

	        // Insert data into loan table
	        String insertLoanQuery = "INSERT INTO loan (member_id, amount, payment_period_in_months, loan_application_number) VALUES (?, ?, ?, ?)";
	        try (PreparedStatement insertLoanStmt = connection.prepareStatement(insertLoanQuery)) {
	            insertLoanStmt.setInt(1, 1); // Assuming the member ID is 1
	            insertLoanStmt.setBigDecimal(2, BigDecimal.valueOf(5000.00));
	            insertLoanStmt.setInt(3, 12);
	            insertLoanStmt.setString(4, "L001");
	            insertLoanStmt.executeUpdate();
	        }
	        */
	    }

    private void createTables() {
	        // Table: member
	        
            try {
                String createMemberTable = "CREATE TABLE member (" +
	                "id INT AUTO_INCREMENT PRIMARY KEY, " +
	                "username VARCHAR(50) NOT NULL, " +
	                "password VARCHAR(25) NOT NULL, " +
	                "member_number VARCHAR(25) NOT NULL, " +
	                "phone_number VARCHAR(20) NOT NULL " +
	                ")";
	        con.createStatement().execute(createMemberTable);
            } catch (Exception e) {
                System.out.println("Failed to execute sql statement");
                System.out.println(e);
            }
	        /*

	        // Table: deposit
	        String createDepositTable = "CREATE TABLE deposit (" +
	                "id INT AUTO_INCREMENT PRIMARY KEY, " +
	                "member_id INT NOT NULL, " +
	                "amount DECIMAL(10, 2) NOT NULL, " +
	                "date_deposited DATE NOT NULL, " +
	                "receipt_number VARCHAR(255) NOT NULL, " +
	                "FOREIGN KEY (member_id) REFERENCES member(id) " +
	                ")";
	        connection.createStatement().execute(createDepositTable);

	        // Table: loan
	        String createLoanTable = "CREATE TABLE loan (" +
	                "id INT AUTO_INCREMENT PRIMARY KEY, " +
	                "member_id INT NOT NULL, " +
	                "amount DECIMAL(10, 2) NOT NULL, " +
	                "payment_period_in_months INT NOT NULL, " +
	                "loan_application_number VARCHAR(255) NOT NULL, " +
	                "status ENUM('pending', 'granted', 'rejected') DEFAULT 'pending', " +
	                "FOREIGN KEY (member_id) REFERENCES member(id) " +
	                ")";
	        connection.createStatement().execute(createLoanTable);
	         */
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

            String request = input.readLine();

            // Process the request
            String response = processRequest(request, output, input);

            // Send the response back to the client
            output.println(response);

            socket.close();
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
                // TODO: Implement deposit command
                break;
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
                String[] memberInfoStrings = {memberNumber, phoneNumber};

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
}

