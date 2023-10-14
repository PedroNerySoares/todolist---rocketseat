package br.com.pedronery.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pedronery.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentTime = LocalDateTime.now();
        if (currentTime.isAfter(taskModel.getStartAt()) || currentTime.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data Inicio deve ser maior que a data atual");
        }
        if (taskModel.getEndAt().isAfter(taskModel.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data Inicio deve ser menor que a data de");
        }

        var task = taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskModel);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = taskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }

    @PutMapping("/{idTask}")
    public ResponseEntity update(@RequestBody TaskModel taskBody, @PathVariable UUID idTask,
            HttpServletRequest request) {
        var task = taskRepository.findById(idTask).orElse(null);
        var idUser = request.getAttribute("idUser");

          if (task==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada!");
        }

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskBody, task);

        var taskUpdated = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(taskUpdated);
    }

}
