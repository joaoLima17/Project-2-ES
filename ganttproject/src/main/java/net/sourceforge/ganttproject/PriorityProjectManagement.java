package net.sourceforge.ganttproject;

import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;
import net.sourceforge.ganttproject.task.Task;


public class PriorityProjectManagement {

  private static final int JANUARY = 1;
  private static final int FEBRUARY = 2;
  private static final int MARCH = 3;
  private static final int APRIL = 4;
  private static final int MAY = 5;
  private static final int JUNE = 6;
  private static final int JULY = 7;
  private static final int AUGUST = 8;
  private static final int SEPTEMBER = 9;
  private static final int OCTOBER = 10;
  private static final int NOVEMBER = 11;
  private static final int DECEMBER = 12;

  private GanttCalendar earlyStart, lateStart, earlyFinish, lateFinish;
  private float duration, slack;

  private Task task;

  private boolean isOver;

  private int[] months = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};




  public PriorityProjectManagement(Task task){
    this.task = task;
    this.duration = task.getDuration().getValue();
    this.earlyStart = task.getStart();
    this.earlyFinish = task.getEnd();

    //isto tem de ser chamado antes de o objeto ser criado
    if(task.getCompletionPercentage() == 100){
      isOver = true;
    } else {
      isOver = false;
    }



  }

  public void setLateFinish(GanttCalendar lateFinish){
    this.lateFinish = lateFinish;
  }

  public void calculateSlack() {
    //slack = lateStart - earlyStart
    int lateStartMonth, earlyStartMonth;
    if(lateStart.getMonth() == 2){

    }
    int totalDays = 0;
    for(int i = 0; i < 12; i++){
      if(lateStart.getMonth() == i){

      }
    }



    slack = ((lateStart.getYear() - earlyStart.getYear()) * 365) + ((lateStart.getMonth() - earlyStart.getMonth()) * 30) + (lateStart.getDay() - earlyStart.getDay());


  }

  public void calculateLateStart() { //lateFinish - duration;

    if(lateFinish.getDay() - duration >= 1){
      //lateStart = new GanttCalendar(lateFinish.getDay()-duration, lateFinish.getMonth(), lateFinish.getYear(), local);
    } else {
      float day;
      if(lateFinish.getMonth()-1 == 2){
        if(lateFinish.getYear() % 4 == 0){
          day = 29 - (lateFinish.getDay() - duration);
        } else {
          day = 28 - (lateFinish.getDay() - duration);
        }
      } else if (lateFinish.getMonth()-1 == 4 || lateFinish.getMonth()-1 == 6 || lateFinish.getMonth()-1 == 9 ||
        lateFinish.getMonth()-1 == 11){
        day = 30 - (lateFinish.getDay() - duration);
      } else {
        day = 31 - (lateFinish.getDay() - duration);
      }
      //lateStart = new GanttCalendar(day, lateFinish.getMonth()-1, lateFinish.getYear(), local);
    }

  }


  public float getDuration() {
    return duration;
  }

  public float getSlack() {
    return slack;
  }

  public Task getTask(){
    return task;
  }

  public GanttCalendar getEarlyFinish() {
    return earlyFinish;
  }

  public GanttCalendar getEarlyStart() {
    return earlyStart;
  }

  public GanttCalendar getLateStart(){
    return lateStart;
  }

  public GanttCalendar getLateFinish() {
    return lateFinish; //LST(i+1)
  }

}
