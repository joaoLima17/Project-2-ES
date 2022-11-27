package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskImpl;
import net.sourceforge.ganttproject.PriorityProjectManagement;

import java.util.ArrayList;
import java.util.List;

public class PriorityToBeAssignedTask {

  //lists
  private final List<Task> allTasks = new ArrayList();

  public PriorityToBeAssignedTask(){
    //atualiza lista de tasks
    //calcula prioridade
    CalculatePriority();
    //organiza nova lista
  }

  public List<Task> getTasksInOrderToBeAssigned() {
    return allTasks;//return lista organizada
  }

  private void CalculatePriority(){
    for(int i = 0; i < allTasks.size(); i++){
      if(allTasks.get(i).getStart().after(allTasks.get(i+1).getStart())){



      }
    }

  }


}
