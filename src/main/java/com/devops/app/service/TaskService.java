package com.devops.app.service;

import com.devops.app.dto.PagedResponse;
import com.devops.app.dto.TaskRequest;
import com.devops.app.dto.TaskResponse;
import com.devops.app.exception.TaskNotFoundException;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository, TaskEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<TaskResponse> findAll(Pageable pageable) {
        Page<TaskResponse> page = taskRepository.findAll(pageable).map(TaskResponse::from);
        return PagedResponse.from(page);
    }

    public PagedResponse<TaskResponse> search(String title, Task.TaskStatus status,
                                               Task.Priority priority, Pageable pageable) {
        Page<TaskResponse> page = taskRepository.search(title, status, priority, pageable)
                                                .map(TaskResponse::from);
        return PagedResponse.from(page);
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse findById(Long id) {
        log.debug("Fetching task id={}", id);
        return taskRepository.findById(id)
                             .map(TaskResponse::from)
                             .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse create(TaskRequest request) {
        log.info("Creating task: {}", request.title());
        Task task = new Task(request.title(), request.description(), request.status(), request.priority());
        Task saved = taskRepository.save(task);
        eventPublisher.onTaskCreated(saved);
        return TaskResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponse update(Long id, TaskRequest request) {
        log.info("Updating task id={}", id);
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        Task saved = taskRepository.save(task);
        if (saved.getStatus() == Task.TaskStatus.DONE) {
            eventPublisher.onTaskCompleted(id);
        }
        return TaskResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public void delete(Long id) {
        log.info("Deleting task id={}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
        eventPublisher.onTaskDeleted(id);
    }

    public Map<String, Long> getStatusSummary() {
        return taskRepository.countByStatus().stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
    }
}
