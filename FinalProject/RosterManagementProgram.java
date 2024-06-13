package FinalProject;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class RosterManagementProgram {
    private static final String PASSWORD = "0920"; // Roster Management 접근 비밀번호
    private static final String[] TITLE_ARRAY = {
            "Pastor", "Probation Pastor", "Junior Pastor", "Elder", "Exhorter", "Deacon", "Layman", "Student"
    };
    private static final String DATE_FILE = "lastRunDate.txt";
    private static Map<Long, Person> people = new HashMap<>();
    private static Map<String, List<Person>> rosters = new HashMap<>();
    private static Set<Long> attendance = new HashSet<>();
    private static Scanner scanner = new Scanner(System.in);
    private static boolean isModified = false; // 명단이 수정되었는지 여부를 추적하는 변수
    private static final String ATTENDANCE_FILE = "Attendance.dat";
    private static final String ROSTER_FILE = "Roster.txt";
    private static LocalDate programStartDate;

    public static void main(String[] args) {
        programStartDate = LocalDate.now();
        initializeRosters();
        loadRosterFromFile("Roster.dat");
        loadAttendanceFromFile(ATTENDANCE_FILE);

        if (isProgramDateChanged()) {
            attendance.clear(); // 출석 초기화
            System.out.println("Attendance reset because program date changed.");
        }

        while (true) {
            System.out.println("Select an option: (1) Attendance, (2) Roster Management, (3) Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    handleAttendance();
                    break;
                case 2:
                    if (checkPassword()) {
                        manageRoster();
                    } else {
                        System.out.println("Incorrect password. Returning to main menu.");
                    }
                    break;
                case 3:
                    if (confirmExit()) {
                        saveAttendanceToFile(ATTENDANCE_FILE);
                        sortAndSaveRosterToFile(ROSTER_FILE); // Roster.txt 파일 정렬 후 저장
                        saveProgramDate();
                        System.out.println("Exiting program...");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void initializeRosters() {
        rosters.put("Senior", new ArrayList<>());
        rosters.put("Young Adult", new ArrayList<>());
        rosters.put("Youth", new ArrayList<>());
        rosters.put("Preschool", new ArrayList<>());
        rosters.put("Kindergarten", new ArrayList<>());
    }

    private static void loadRosterFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue; // Skip malformed lines
                }
                String name = parts[0];
                int age = Integer.parseInt(parts[1]);
                String title = parts[2];
                long ssn = Long.parseLong(parts[3]);
                String branch = parts[4];

                Person person = new Person(name, age, title, ssn);
                rosters.get(branch).add(person);
                people.put(ssn, person);
            }
            System.out.println("Roster loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading roster from file: " + e.getMessage());
        }
    }

    private static void handleAttendance() {
        System.out.println("Enter the unique number (SSN):");
        long uniqueNumber = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        Person person = people.get(uniqueNumber);
        if (person != null) {
            if (attendance.contains(uniqueNumber)) {
                System.out.println("This person has already been marked as present.");
            } else {
                System.out.println(person);
                System.out.println("Is this you? (yes/no)");
                if (scanner.nextLine().equalsIgnoreCase("yes")) {
                    attendance.add(uniqueNumber);
                    System.out.println("Attendance confirmed.");
                }
            }
        } else {
            System.out.println("Invalid number!");
        }
    }

    private static boolean checkPassword() {
        System.out.println("Enter password to access Roster Management:");
        String inputPassword = scanner.nextLine();
        return PASSWORD.equals(inputPassword);
    }

    private static void manageRoster() {
        while (true) {
            System.out.println("Select an option: (1) Add, (2) Remove, (3) Change, (4) View, (5) Save, (6) Leave");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addPerson();
                    break;
                case 2:
                    removePerson();
                    break;
                case 3:
                    changePerson();
                    break;
                case 4:
                    viewRoster();
                    break;
                case 5:
                    saveRoster();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void addPerson() {
        System.out.println("Enter name:");
        String name = scanner.nextLine();
        System.out.println("Enter age:");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.println(
                "Enter title (Pastor, Probation Pastor, Junior Pastor, Elder, Exhorter, Deacon, Layman, Student):");
        String title = scanner.nextLine();
        while (!isValidTitle(title)) {
            System.out.println("Invalid title! Please enter a valid title:");
            title = scanner.nextLine();
        }
        System.out.println("Enter SSN:");
        long ssn = scanner.nextLong();
        scanner.nextLine(); // Consume newline
        String branch = selectBranch();

        Person newPerson = new Person(name, age, title, ssn);
        rosters.get(branch).add(newPerson);
        people.put(ssn, newPerson);
        isModified = true; // 명단이 수정되었음을 표시
        System.out.println("Person added to " + branch);
    }

    private static boolean isValidTitle(String title) {
        for (String validTitle : TITLE_ARRAY) {
            if (validTitle.equals(title)) {
                return true;
            }
        }
        return false;
    }

    private static void removePerson() {
        System.out.println("Enter the unique number (SSN) to remove:");
        long uniqueNumber = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        Person person = people.remove(uniqueNumber);

        if (person != null) {
            for (List<Person> list : rosters.values()) {
                list.remove(person);
            }
            isModified = true; // 명단이 수정되었음을 표시
            System.out.println("Person removed.");
        } else {
            System.out.println("Person not found.");
        }
    }

    private static void changePerson() {
        System.out.println("Enter the unique number (SSN) to change:");
        long uniqueNumber = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        Person person = people.get(uniqueNumber);

        if (person != null) {
            for (List<Person> list : rosters.values()) {
                list.remove(person);
            }
            addPerson(); // Add a new person to replace the old one
        } else {
            System.out.println("Person not found.");
        }
    }

    private static void viewRoster() {
        String branch = selectBranch();
        List<Person> roster = rosters.get(branch);
        for (Person person : roster) {
            String attendanceStatus = attendance.contains(person.getSsn()) ? "Present" : "Absent";
            System.out.println(person + ", Attendance: " + attendanceStatus);
        }
    }

    private static void saveRoster() {
        try (PrintWriter datWriter = new PrintWriter(new FileWriter("Roster.dat"));
                PrintWriter txtWriter = new PrintWriter(new FileWriter("Roster.txt"))) {
            for (Map.Entry<String, List<Person>> entry : rosters.entrySet()) {
                String branch = entry.getKey();
                for (Person person : entry.getValue()) {
                    datWriter.println(person.getName() + "," + person.getAge() + "," + person.getTitle() + ","
                            + person.getSsn() + "," + branch);
                    txtWriter
                            .println(person.getName() + "," + person.getAge() + "," + person.getTitle() + "," + branch);
                }
            }
            isModified = false; // 저장 후에는 수정되지 않은 상태로 설정
            System.out.println("Roster saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving roster to file: " + e.getMessage());
        }
    }

    private static String selectBranch() {
        System.out.println("Select a branch: (1) Senior, (2) Young Adult, (3) Youth, (4) Preschool, (5) Kindergarten");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                return "Senior";
            case 2:
                return "Young Adult";
            case 3:
                return "Youth";
            case 4:
                return "Preschool";
            case 5:
                return "Kindergarten";
            default:
                System.out.println("Invalid choice! Defaulting to 'Senior'");
                return "Senior";
        }
    }

    private static boolean confirmExit() {
        System.out.println("Do you want to save before exiting? (yes/no)");
        return scanner.nextLine().equalsIgnoreCase("yes");
    }

    @SuppressWarnings("unchecked")
    private static void loadAttendanceFromFile(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            attendance = (Set<Long>) ois.readObject();
            System.out.println("Attendance loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading attendance from file: " + e.getMessage());
        }
    }

    private static void saveAttendanceToFile(String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(attendance);
            System.out.println("Attendance saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving attendance to file: " + e.getMessage());
        }
    }

    private static void sortAndSaveRosterToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            List<Person> sortedPeople = new ArrayList<>(people.values());
            sortedPeople.sort(Comparator.comparingInt(p -> getBranchOrder(p.getTitle())));

            for (Person person : sortedPeople) {
                writer.println(person.getName() + "," + person.getAge() + "," + person.getTitle() + ","
                        + person.getSsn());
            }
            System.out.println("Sorted roster saved to " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving sorted roster to file: " + e.getMessage());
        }
    }

    private static int getBranchOrder(String title) {
        for (int i = 0; i < TITLE_ARRAY.length; i++) {
            if (TITLE_ARRAY[i].equals(title)) {
                return i;
            }
        }
        return TITLE_ARRAY.length;
    }

    private static int getTitleIndex(String title) {
        for (int i = 0; i < TITLE_ARRAY.length; i++) {
            if (TITLE_ARRAY[i].equals(title)) {
                return i;
            }
        }
        return -1; // Invalid title
    }

    private static void saveProgramDate() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATE_FILE))) {
            writer.println(programStartDate);
            System.out.println("Program date saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving program date to file: " + e.getMessage());
        }
    }

    private static boolean isProgramDateChanged() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                LocalDate lastRunDate = LocalDate.parse(line);
                return !lastRunDate.equals(programStartDate);
            }
        } catch (IOException e) {
            System.err.println("Error reading program date from file: " + e.getMessage());
        }
        return true; // If date file is not found or unreadable, assume date has changed
    }
}
