package org.example.myException;

public class WrongUrlException extends Exception{
    public WrongUrlException(String message){
        super(message);
    }
}
