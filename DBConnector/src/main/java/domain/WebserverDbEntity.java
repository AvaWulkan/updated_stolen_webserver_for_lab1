package domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "webserver_db", schema = "webserver_db")
public class WebserverDbEntity {
    private int id;
    private String name;
    private Integer age;
    private String programmer;

    @Id
    @GeneratedValue
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "age")
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Basic
    @Column(name = "programmer")
    public String getProgrammer() {
        return programmer;
    }

    public void setProgrammer(String programmer) {
        this.programmer = programmer;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebserverDbEntity that = (WebserverDbEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(age, that.age) && Objects.equals(programmer, that.programmer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, programmer);
    }

    @Override
    public String toString() {
        return "[Id: " + id + ", Name: " + name + ", age: " + age + ", Programmer: " + programmer + "]\n";
    }
}
