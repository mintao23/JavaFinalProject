package FinalProject;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class RosterManagementProgram {
    private static final String PASSWORD = "0920"; // Roster Management 접근 비밀번호
    private static final String[] TITLE_ARRAY = {
            "Pastor", "Probation Pastor", "Junior Pastor", "Elder", "Exhorter", "Deacon", "Layman", "Student"
    };
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
                        System.out.println("Exiting program...");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
            if (isProgramDateChanged()) {
                attendance.clear(); // 출석 초기화
                System.out.println("Attendance reset because program date changed.");
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
                System.out.println("Invalid choice! Defaulting to Senior.");
                return "Senior";
        }
    }

    private static boolean confirmExit() {
        if (isModified) {
            System.out.println("There are unsaved changes. Do you want to save before exiting? (yes/no)");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                saveRoster();
            }
        }
        System.out.println("Are you sure you want to exit? (yes/no)");
        return scanner.nextLine().equalsIgnoreCase("yes");
    }

    private static void loadAttendanceFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                long ssn = Long.parseLong(line);
                attendance.add(ssn);
            }
            System.out.println("Attendance loaded successfully.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading attendance from file: " + e.getMessage());
        }
    }

    private static void saveAttendanceToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Long ssn : attendance) {
                writer.println(ssn);
            }
            System.out.println("Attendance saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving attendance to file: " + e.getMessage());
        }
    }

    private static void sortAndSaveRosterToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // 소속을 순서에 따라 정렬하기 위한 Comparator
            Comparator<String> branchComparator = Comparator.comparingInt(RosterManagementProgram::getBranchOrder);

            // 소속을 순서에 따라 정렬된 키 집합 생성
            List<String> sortedBranches = new ArrayList<>(rosters.keySet());
            sortedBranches.sort(branchComparator);

            for (String branch : sortedBranches) {
                List<Person> roster = rosters.get(branch);

                // title의 index가 낮은 순서대로 정렬
                roster.sort(Comparator.comparingInt(person -> getTitleIndex(person.getTitle())));

                writer.println("Branch: " + branch);
                for (Person person : roster) {
                    writer.println(person.getName() + "," + person.getAge() + "," + person.getTitle());
                }
                writer.println(); // 소속 간의 간격을 위한 빈 줄 추가
            }
            System.out.println("Roster sorted and saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving sorted roster to file: " + e.getMessage());
        }
    }

    // 소속의 순서를 반환하는 메서드
    private static int getBranchOrder(String branch) {
        switch (branch) {
            case "Senior":
                return 1;
            case "Young Adult":
                return 2;
            case "Youth":
                return 3;
            case "Preschool":
                return 4;
            case "Kindergarten":
                return 5;
            default:
                return Integer.MAX_VALUE; // 정의되지 않은 소속은 가장 큰 값으로 설정하여 맨 뒤로 이동
        }
    }

    private static int getTitleIndex(String title) {
        for (int i = 0; i < TITLE_ARRAY.length; i++) {
            if (TITLE_ARRAY[i].equals(title)) {
                return i;
            }
        }
        return TITLE_ARRAY.length; // 정의되지 않은 title은 가장 높은 index 반환
    }
    private static boolean isProgramDateChanged() {
        // 프로그램 실행 중인 날짜와 프로그램 시작일을 비교하여 변경 여부 판단
        return !LocalDate.now().isEqual(programStartDate);
    }
}