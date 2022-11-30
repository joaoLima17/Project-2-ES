package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.task.Task;

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
      if(allTasks.get(i).getCompletionPercentage() < 100) {
        allTasksDetailed.add(new PriorityProjectManagement(allTasks.get(i)));
      }
    }
  }

  public List<Task> getTasksInOrderToBeAssigned() {
    List<Task> finalTasks = new ArrayList<>();
    for(int i = 0; i < allTasksInOrder.size(); i++){
      finalTasks.add(allTasksInOrder.get(i).getTask());
    }
    return finalTasks;//return lista organizada
  }


  private void CalculatePriority(){
    //slides
    for(int i = allTasksDetailed.size() - 1; i >= 0 ; i--){
      if(i == allTasksDetailed.size()){
        allTasksDetailed.get(i).setLateFinish(allTasksDetailed.get(i).getEarlyFinish());
      } else {
        allTasksDetailed.get(i).setLateFinish(allTasksDetailed.get(i+1).getLateStart()); //LFT(D) = LST(F)
      }
    }

    //Temos de arranjar uma maneira de ordenar a lista, podemos fazer um comparador mas nao sei bem fazer isso
    for(int j = 0; j < allTasksDetailed.size(); j++){
      if(allTasksDetailed.get(j).getLateFinish().before(allTasksDetailed.get(j+1).getLateFinish())){
        allTasksInOrder.add(allTasksDetailed.get(j));
      } else if(allTasksDetailed.get(j).getLateFinish().after(allTasksDetailed.get(j+1).getLateFinish())){

      }
    }


  }


}
