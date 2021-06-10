package coresources;

public class Person {

    private String name;
    private int age;
    private boolean programmer;

    public Person(String name, int age, boolean programmer) {

        this.name = name;
        this.age = age;
        this.programmer = programmer;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean isProgrammer() {
        return programmer;
    }
}
