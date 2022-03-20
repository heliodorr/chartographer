package ru.kontur.intern.chartographer.image;

public class HttpException extends Throwable{

  private int errorCode;

  public HttpException(int errorCode){
    this.errorCode = errorCode;
  }

  public int getErrorCode() {
    return errorCode;
  }

}
