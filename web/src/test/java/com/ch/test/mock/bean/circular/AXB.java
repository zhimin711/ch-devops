package com.ch.test.mock.bean.circular;


public class AXB {

    private BXA BXA;

    private String name;

    public BXA getBXA() {
        return BXA;
    }

    public void setBXA(BXA BXA) {
        this.BXA = BXA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
