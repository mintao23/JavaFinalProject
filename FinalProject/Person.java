package FinalProject;
public class Person {
    private String name;
    private int age;
    private String title;
    private long ssn;

    public Person(String name, int age, String title, long ssn) {
        this.name = name;
        this.age = age;
        this.title = title;
        this.ssn = ssn;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getTitle() {
        return title;
    }

    public long getSsn() {
        return ssn;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Age: " + age + ", Title: " + title + ", SSN: " + ssn;
    }
}