package FinalProject;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class RosterMgmt {
    private static final String PASS = "0920"; // Roster Management 접근 비밀번호
    private static final String[] TITLES = {
            "Pastor", "Probation Pastor", "Junior Pastor", "Elder", "Exhorter", "Deacon", "Layman", "Student"
    };
    private static final String DATE_FILE = "lastRunDate.txt";
    private static Map<Long, Person> people = new HashMap<>();
    private static Map<String, List<Person>> rosters = new HashMap<>();
    private static Set<Long> attendance = new HashSet<>();
    private static Scanner sc = new Scanner(System.in);
    private static boolean isModified = false; // 명단이 수정되었는지 여부를 추적하는 변수
    private static final String ATT_FILE = "Attendance.dat";
    private static final String ROSTER_FILE = "Roster.txt";
    private static LocalDate progStartDate;

    public static void main(String[] args) {
        progStartDate = LocalDate.now();
        initRosters();
        loadRoster("Roster.dat");
        loadAttendance(ATT_FILE);

        if (isDateChanged()) {
            attendance.clear(); // 출석 초기화
            System.out.println("Attendance reset because program date changed.");
        }

        while (true) {
            System.out.println("Select an option: (1) Attendance, (2) Roster Mgmt, (3) Exit");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    handleAttendance();
                    break;
                case 2:
                    if (checkPass()) {
                        manageRoster();
                    } else {
                        System.out.println("Incorrect password. Returning to main menu.");
                    }
                    break;
                case 3:
                    if (confirmExit()) {
                        saveAttendance(ATT_FILE);
                        saveRosterSorted(ROSTER_FILE); // Roster.txt 파일 정렬 후 저장
                        saveDate();
                        System.out.println("Exiting program...");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void initRosters() {
        rosters.put("Senior", new ArrayList<>());
        rosters.put("Young Adult", new ArrayList<>());
        rosters.put("Youth", new ArrayList<>());
        rosters.put("Preschool", new ArrayList<>());
        rosters.put("Kindergarten", new ArrayList<>());
    }

    private static void loadRoster(String fileName) {
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
        long ssn = sc.nextLong();
        sc.nextLine(); // Consume newline

        Person person = people.get(ssn);
        if (person != null) {
            if (attendance.contains(ssn)) {
                System.out.println("This person has already been marked as present.");
            } else {
                System.out.println(person);
                System.out.println("Is this you? (yes/no)");
                if (sc.nextLine().equalsIgnoreCase("yes")) {
                    attendance.add(ssn);
                    System.out.println("Attendance confirmed.");
                }
            }
        } else {
            System.out.println("Invalid number!");
        }
    }

    private static boolean checkPass() {
        System.out.println("Enter password to access Roster Mgmt:");
        String inputPass = sc.nextLine();
        return PASS.equals(inputPass);
    }

    private static void manageRoster() {
        while (true) {
            System.out.println("Select an option: (1) Add, (2) Remove, (3) Change, (4) View, (5) Save, (6) Leave");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

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
        String name = sc.nextLine();
        System.out.println("Enter age:");
        int age = sc.nextInt();
        sc.nextLine(); // Consume newline
        System.out.println(
                "Enter title (Pastor, Probation Pastor, Junior Pastor, Elder, Exhorter, Deacon, Layman, Student):");
        String title = sc.nextLine();
        while (!isValidTitle(title)) {
            System.out.println("Invalid title! Please enter a valid title:");
            title = sc.nextLine();
        }
        System.out.println("Enter SSN:");
        long ssn = sc.nextLong();
        sc.nextLine(); // Consume newline
        String branch = selectBranch();

        Person newPerson = new Person(name, age, title, ssn);
        rosters.get(branch).add(newPerson);
        people.put(ssn, newPerson);
        isModified = true; // 명단이 수정되었음을 표시
        System.out.println("Person added to " + branch);
    }

    private static boolean isValidTitle(String title) {
        for (String validTitle : TITLES) {
            if (validTitle.equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    private static void removePerson() {
        System.out.println("Enter the unique number (SSN) to remove:");
        long ssn = sc.nextLong();
        sc.nextLine(); // Consume newline

        Person person = people.remove(ssn);

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
        long ssn = sc.nextLong();
        sc.nextLine(); // Consume newline

        Person person = people.get(ssn);

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
            String attStatus = attendance.contains(person.getSsn()) ? "Present" : "Absent";
            System.out.println(person + ", Attendance: " + attStatus);
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
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline

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
                System.out.println("Invalid choice! Defaulting to Senior.");
                return "Senior";
        }
    }

    private static void loadAttendance(String fileName) {
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            while (fileScanner.hasNextLong()) {
                long ssn = fileScanner.nextLong();
                attendance.add(ssn);
            }
            System.out.println("Attendance data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.err.println("Attendance file not found. Starting with empty attendance.");
        }
    }

    private static boolean isDateChanged() {
        LocalDate savedDate = LocalDate.now();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATE_FILE))) {
            String savedDateString = reader.readLine();
            savedDate = LocalDate.parse(savedDateString);
        } catch (IOException e) {
            System.err.println("Error reading date file: " + e.getMessage());
        }

        return !progStartDate.equals(savedDate);
    }

    private static void saveAttendance(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (long ssn : attendance) {
                writer.println(ssn);
            }
            System.out.println("Attendance data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving attendance data: " + e.getMessage());
        }
    }

    private static void saveRosterSorted(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            List<Person> sortedList = new ArrayList<>();
            for (List<Person> list : rosters.values()) {
                sortedList.addAll(list);
            }
            sortedList.sort(Comparator.comparing(Person::getName));

            for (Person person : sortedList) {
                writer.println(person.getName() + "," + person.getAge() + "," + person.getTitle() + "," +
                        person.getSsn());
            }
            System.out.println("Sorted roster saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving sorted roster: " + e.getMessage());
        }
    }

    private static void saveDate() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATE_FILE))) {
            writer.println(progStartDate);
            System.out.println("Program date saved.");
        } catch (IOException e) {
            System.err.println("Error saving program date: " + e.getMessage());
        }
    }

    private static boolean confirmExit() {
        if (isModified) {
            System.out.println("Data has been modified. Do you want to save before exiting? (yes/no)");
            String answer = sc.nextLine();
            if (answer.equalsIgnoreCase("yes")) {
                saveRoster();
                saveAttendance(ATT_FILE);
                saveDate();
            }
        }
        return true;
    }
}
