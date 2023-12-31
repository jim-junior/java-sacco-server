import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server2 {
    private static final String DB_HOST = "jdbc:mysql://localhost:3306/sacco";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
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
                System.out.println("Request from client: " + request);

                if (request.equals("logout")) {
                    socket.close();
                    break;
                }

                // Process the request
                String response;

                try {
                    response = processRequest(request, output, input);
                } catch (Exception e) {
                    System.out.println(e);
                    response = "An Error Occured";
                }

                // Send the response back to the client
                output.println(response);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processRequest(String request, PrintWriter output, BufferedReader input) throws Exception {
        String[] commandParts = request.split(" ");
        String command = commandParts[0].toLowerCase();
        System.out.println("Command: " + command);

        switch (command) {
            case "login":
                return processLogin(commandParts, output, input);
            case "deposit":
                return deposit(commandParts);
            case "checkstatement":
                return checkLoanStatement(commandParts, output, input);
            case "requestloan":
                // TODO: Implement requestloan command
                break;
            case "loanrequeststatus":
                return requestLoanStatus(commandParts, output, input);
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

    public String requestLoanStatus(String[] commandParts, PrintWriter output, BufferedReader input) throws Exception {
        System.out.println(">>> Request loan status");
        int loanApplicationNumber = Integer.parseInt(commandParts[1]);
        String loanStatus = getLoanRequestStatus(loanApplicationNumber);
        System.out.println("Loan status: " + loanStatus);

        if (loanStatus.contains("Loan Status: Granted")) {
            output.println("Do you want to accept or reject the loan? (Type 'accept' or 'reject')");
            String decision = input.readLine();
            System.out.println("Client decision: " + decision);

            if (decision.equalsIgnoreCase("accept")) {
                acceptLoanRequest(loanApplicationNumber);
                output.println("Loan request accepted. Details:");
                output.println(getLoanRequestStatus(loanApplicationNumber));
            } else if (decision.equalsIgnoreCase("reject")) {

                rejectLoanRequest(loanApplicationNumber);
                output.println("Loan request rejected.");
            } else {
                output.println("Invalid decision. Loan request not updated.");
            }
            return "Done: Success";
        } else {
            System.out.println(">>> Invalid if statement");
            return "Done: Failed";
        }
    }

    private void acceptLoanRequest(int loanApplicationNumber) {
        // Database update query to set loan status as accepted
        String sql = "UPDATE loanRequests SET loanStatus = 'Accepted' WHERE loanApllicationNumber = ?";
        try {

            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, loanApplicationNumber);
            statement.executeUpdate();
            System.out.println("Loan request accepted for loan application number: " + loanApplicationNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void rejectLoanRequest(int loanApplicationNumber) {
        // Database update query to set loan status as rejected
        String sql = "UPDATE loanRequests SET loanStatus = 'Rejected' WHERE loanApllicationNumber = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, loanApplicationNumber);
            statement.executeUpdate();
            System.out.println("Loan request rejected for loan application number: " + loanApplicationNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getLoanRequestStatus(int loanApplicationNumber) {
        String loanStatus = "";
        String expectedInstallments = "";
        String expectedDates = "";

        // Database query to retrieve loan request status and related information
        String sql = "SELECT loanStatus, expectedInstallments, expectedDates FROM loanRequests WHERE loanApllicationNumber = ?";

        try {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, loanApplicationNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                loanStatus = resultSet.getString("loanStatus"); // column label stands for column name
                expectedInstallments = resultSet.getString("expectedInstallments");
                expectedDates = resultSet.getString("expectedDates");
            } else {
                return "  loan application number not found ";
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
            return ("an error occurred when retrieving data");
        }

        return "Expected Installments: " + expectedInstallments + "\nExpected Dates: " + expectedDates
                + "\nLoan Status: " + loanStatus;
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
            if (commandArgs.length != 4) {
                return "Invalid deposit command";
            }
            String receiptId = commandArgs[1];
            String date = commandArgs[2];
            Integer amount = Integer.parseInt(commandArgs[3]);

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
            System.out.println(e);
            return "An Error Occured";
        }
    }

    public String checkLoanStatement(String[] command, PrintWriter output, BufferedReader input) {
        try {

            int memberid = Integer.parseInt(command[1]);
            checkstatement(memberid, command[2], command[3], output);
            loanstatus(memberid, command[2], command[3], output);
            percentage(memberid, command[2], command[3], output);
            loanpercentage(memberid, command[2], command[3], output);
            return "Done: Success";

        } catch (Exception e) {
            return "Done: An Error Occured";
        }

    }

    public void checkstatement(int userid, String datefrom, String dateto, PrintWriter writer) {
        try {
            String query = "SELECT * FROM deposit WHERE depositDate BETWEEN '" + datefrom + "' AND '" + dateto
                    + "' AND status='redeemed' AND memberID=" + userid + "";
            // String query = "SELECT * FROM deposit WHERE depositDate BETWEEN
            // '"+datefrom+"' AND '"+dateto+"' AND depositStatus='redeemed' AND
            // memberID="+userid+"";
            Statement statement = con.createStatement();
            ResultSet contribution = statement.executeQuery(query);

            while (contribution.next()) {
                String depositDate = contribution.getString("depositDate");
                int amount = contribution.getInt("amount");
                int totalAmount = 0;
                totalAmount = 0 + amount;

                writer.println("Deposit Date: " + depositDate);
                writer.println("Amount: " + amount);
                writer.println("contribution is: " + totalAmount);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loanstatus(int userid, String datefrom, String dateto, PrintWriter writer) throws Exception {

        String query = "SELECT * FROM loan WHERE loandepositdate BETWEEN '" + datefrom + "' AND '" + dateto
                + "' AND status='redeemed'AND memberID=" + userid;

        // String query = "SELECT * FROM deposit WHERE depositDate BETWEEN
        // '"+datefrom+"' AND '"+dateto+"' AND depositStatus='redeemed' AND
        // memberID="+userid+"";
        Statement statement = con.createStatement();
        ResultSet contribution = statement.executeQuery(query);

        while (contribution.next()) {

            String loandepositDate = contribution.getString("loandepositdate");
            int amountdeposited = contribution.getInt("amountdeposited");

            writer.println("loan Datedeposit: " + loandepositDate);
            writer.println("amountdeposited: " + amountdeposited);
            writer.println();
        }
    }

    public void loanpercentage(int userid, String datefrom, String dateto, PrintWriter writer) throws Exception {

        String query = "SELECT memberID, COUNT(*) AS loandepositTimes, SUM(amountdeposited) AS loanamountdeposited "
                +
                "FROM loan " +
                // "WHERE memberID = 101"+
                "GROUP BY memberID";

        Statement statement = con.createStatement();
        ResultSet contribution = statement.executeQuery(query);

        while (contribution.next()) {
            int memberID = contribution.getInt("memberID");
            if (userid == memberID) {
                int loandepositTimes = contribution.getInt("loandepositTimes");
                int loanamountDeposited = contribution.getInt("loanamountdeposited");
                double percentage;
                percentage = (double) loandepositTimes / 12 * 100;

                writer.println("loanamountDeposited: " + loanamountDeposited);
                writer.println("depositTimes: " + loandepositTimes);
                writer.println("percentage contribution loan: " + percentage);

                writer.flush();
                System.out.println();
            }

        }
    }

    public void percentage(int userid, String datefrom, String dateto, PrintWriter writer) throws Exception {
        String query = "SELECT memberID, COUNT(*) AS depositTimes, SUM(amount) AS totalAmountDeposited " +
                "FROM deposit " +
                // "WHERE memberID = 101"+
                "GROUP BY memberID";

        Statement statement = con.createStatement();
        ResultSet contribution = statement.executeQuery(query);

        while (contribution.next()) {
            int memberID = contribution.getInt("memberID");
            if (userid == memberID) {
                int depositTimes = contribution.getInt("depositTimes");
                int totalAmountDeposited = contribution.getInt("totalAmountDeposited");
                double percentage;
                percentage = (double) depositTimes / 12 * 100;

                writer.println("totalAmountDepositedonloan: " + totalAmountDeposited);
                writer.println("depositTimes: " + depositTimes);
                writer.println("percentage contribution: " + percentage);

                writer.flush();
                System.out.println();
            }

        }
    }
}
