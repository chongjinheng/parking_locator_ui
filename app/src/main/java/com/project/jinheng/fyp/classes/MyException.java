package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 10/29/2014.
 */
public class MyException extends Exception{
    private final ErrorStatus error;

    public MyException(ErrorStatus error,  String errorMessage){
        super(errorMessage);
        this.error = error;
    }

    public ErrorStatus getError() {
        return error;
    }

}
