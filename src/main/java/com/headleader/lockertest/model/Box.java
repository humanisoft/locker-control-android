package com.headleader.lockertest.model;

public class Box implements Cloneable{
    public int ID;
    public boolean FlagOpened = false;
    public boolean FlagInfrared = false;

    public Box() {

    }
    @Override
    public Object clone()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }
}
