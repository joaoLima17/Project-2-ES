package net.sourceforge.ganttproject;

import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;

public class PriorityProjectManagement {

  GanttCalendar earlyStart, lateStart, earlyFinish, lateFinish;
  TimeDuration duration, slack;

  boolean isOver;


  public PriorityProjectManagement(GanttCalendar start, GanttCalendar finish, TimeDuration duration){
    this.duration = duration;
    this.earlyStart = start;
    this.earlyFinish = finish;
    //verificacao se acabou
  }


  public void setLateStart(GanttCalendar lateStart){
    this.lateStart = lateStart;
  }

  public void setLateFinish(GanttCalendar lateFinish){
    this.lateFinish = lateFinish;
  }

  public void setSlack(TimeDuration slack) {
    this.slack = slack;
  }

  public GanttCalendar getEarlyFinish() {
    return earlyFinish;
  }

  public GanttCalendar getEarlyStart() {
    return earlyStart;
  }

  public GanttCalendar getLateFinish() {
    return lateFinish;
  }

  public GanttCalendar getLateStart() {
    return lateStart;
  }

  public TimeDuration getDuration() {
    return duration;
  }

  public TimeDuration getSlack() {
    return slack;
  }

}
