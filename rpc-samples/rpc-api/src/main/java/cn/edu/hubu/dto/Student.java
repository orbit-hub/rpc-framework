package cn.edu.hubu.dto;

import java.io.Serializable;

/**
 * @Author hxy
 * @Date 2022/4/6
 */

public class Student implements Serializable {
    private static final long serialVersionUID = 42L;
    int no;
    String name;
    public Student() {
    }
    public Student(int no, String name) {
        this.no = no;
        this.name = name;
    }
    public int getNo() {
        return no;
    }
    public void setNo(int no) {
        this.no = no;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return "Student{" +
                "no=" + no +
                ", name='" + name + '\'' +
                '}';
    }
}
