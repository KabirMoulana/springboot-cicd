package com.devops.app.service;

import com.devops.app.model.Task;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public TaskService() {
        save(new Task(null, "Bootstrap the project", "DONE"));
        save(new Task(null, "Write unit tests", "IN_PROGRESS"));
        save(new Task(null, "Set up CI/CD pipeline", "TODO"));
    }

    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(counter.incrementAndGet());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public boolean deleteById(Long id) {
        return tasks.remove(id) != null;
    }
}
