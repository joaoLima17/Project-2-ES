package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskImpl;

import java.util.ArrayList;
import java.util.List;

public class PriorityToBeAssignedTask {

  //lists
  private final List<Task> AllTasks = new ArrayList();

  public PriorityToBeAssignedTask(){
    //atualiza lista de tasks
    //calcula prioridade
    CalculatePriority();
    //organiza nova lista
  }

  public List<Task> getTasksInOrderToBeAssigned() {
    return AllTasks;//return lista organizada
  }

  private void CalculatePriority(){

  }


}
