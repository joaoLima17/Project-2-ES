package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskImpl;
import net.sourceforge.ganttproject.PriorityProjectManagement;

import java.util.ArrayList;
import java.util.List;

public class PriorityToBeAssignedTask {

  //lists
  private final List<Task> allTasks = new ArrayList();
  private final List<PriorityProjectManagement> allTasksDetailed = new ArrayList();

  private final List<PriorityProjectManagement> allTasksInOrder = new ArrayList();

  public PriorityToBeAssignedTask(){
    //atualiza lista de tasks
    //calcula prioridade
    tasksToDetailedTasks();
    CalculatePriority();
    //organiza nova lista
  }

  private void tasksToDetailedTasks(){
    for(int i = 0; i< allTasks.size(); i++){
      allTasksDetailed.add(new PriorityProjectManagement(allTasks.get(i)));
    }
  }

  public List<Task> getTasksInOrderToBeAssigned() {
    return allTasks;//return lista organizada
  }



  private void CalculatePriority(){
    /*for(int i = 0; i < allTasks.size(); i++){
      if(allTasks.get(i).getStart().before(allTasks.get(i).getStart())){


      }
    }*/

    for(int i = 0; i < allTasksDetailed.size() ; i++){
      allTasksDetailed.get(i).setLateFinish(allTasksDetailed.get(i+1).getLateStart()); //LFT(D) = LST(F)
      allTasksDetailed.get(i).calculateLateStart();//LST(D) = LFT(D) - DURATION(D)

    }

  }


}
