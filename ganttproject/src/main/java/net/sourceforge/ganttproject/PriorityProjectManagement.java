package net.sourceforge.ganttproject;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;
import net.sourceforge.ganttproject.task.Task;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class PriorityProjectManagement {

  private static final int[] MONTHS = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  private GanttCalendar earlyStart, lateStart, earlyFinish, lateFinish;
  private float duration, slack;
  private Task task;
  private boolean isOver;


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
    calculateSlack();
    calculateLateStart();
  }

  private void calculateSlack() {
    //slack = lateStart - earlyStart


    slack =TimeUnit.DAYS.convert(
      lateStart.getTime().getTime()-earlyStart.getTime().getTime(), TimeUnit.DAYS);

    }


  public void calculateLateStart() { //lateFinish - duration;

    Calendar c = Calendar.getInstance();
    c.setTime(lateFinish.getTime());
    c.add(Calendar.DAY_OF_MONTH,(int) duration);

    lateStart =CalendarFactory.createGanttCalendar(c.getTime());
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
