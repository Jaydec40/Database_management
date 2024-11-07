import java.io.*;
import java.sql.*;

public class bonus {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String username = "jadecena";
            String password = "12278163";
            String url = "jdbc:oracle:thin:@oracle.cs.ua.edu:1521:xe";
            Connection conn = DriverManager.getConnection(url,username,password);

            while (true) {
                String ssn = readEntry(reader, "Enter an SSN (or 0 to exit): ");
                if (ssn.equals("0")) {
                    System.out.println("Good Bye!");
                    break;
                }
                       checkEmployee(conn, ssn);
            }

            conn.close();
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading input from the user.");
            e.printStackTrace();
        }
    }

    private static void checkEmployee(Connection conn, String ssn) throws SQLException {
        String empQuery = "SELECT fname, lname, dno FROM Employee WHERE ssn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(empQuery)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No Employee with SSN=" + ssn + ".");
                } else {
                    String fname = rs.getString("fname");
                    String lname = rs.getString("lname");
                    int dno = rs.getInt("dno");
                    String name = fname + " " + lname;
                    checkIfManager(conn, name, dno);
                    checkDependents(conn, ssn, name);
                    checkProjects(conn, ssn, name);
                }
            }
        }
    }

    private static void checkIfManager(Connection conn, String name, int dno) throws SQLException {
        String mgrQuery = "SELECT dname FROM Department WHERE mgrssn = (SELECT ssn FROM Employee WHERE dno = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(mgrQuery)) {
            stmt.setInt(1, dno);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(name + " is not a manager.");
                } else {
                    String dname = rs.getString("dname");
                    System.out.println(name + " is the manager of the " + dname + " department.");
                }
            }
        }
    }

    private static void checkDependents(Connection conn, String ssn, String name) throws SQLException {
        String depQuery = "SELECT dependent_name FROM Dependent WHERE essn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(depQuery)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(name + " has no dependents.");
                } else {
                    System.out.println(name + " has the following dependents:");
                    do {
                        String dependentName = rs.getString("dependent_name");
                        System.out.println("\t" + dependentName);
                    } while (rs.next());
                }
            }
        }
    }

    private static void checkProjects(Connection conn, String ssn, String name) throws SQLException {
        String projQuery = "SELECT pname FROM Project JOIN Works_On ON Project.pnumber = Works_On.pno WHERE essn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(projQuery)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(name + " has not worked on any projects.");
                } else {
                    System.out.println(name + " has worked on the following projects:");
                    do {
                        String projectName = rs.getString("pname");
                        System.out.println("\t" + projectName);
                    } while (rs.next());
                }
            }
        }
    }

    // Utility method to read entries from the console
    private static String readEntry(BufferedReader reader, String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine();
    }
}