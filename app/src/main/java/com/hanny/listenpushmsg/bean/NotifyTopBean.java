package com.hanny.listenpushmsg.bean;

import java.io.Serializable;

public class NotifyTopBean implements Serializable {
  public String title;
  public String content;
  public String packageName;
  public String amount="0";
  public long time;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
