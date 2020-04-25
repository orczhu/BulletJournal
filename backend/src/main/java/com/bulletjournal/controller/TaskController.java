package com.bulletjournal.controller;

import com.bulletjournal.clients.UserClient;
import com.bulletjournal.controller.models.*;
import com.bulletjournal.controller.utils.EtagGenerator;
import com.bulletjournal.es.SearchService;
import com.bulletjournal.notifications.*;
import com.bulletjournal.repository.TaskDaoJpa;
import com.bulletjournal.repository.models.CompletedTask;
import com.bulletjournal.repository.models.TaskContent;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TaskController {

    protected static final String TASKS_ROUTE = "/api/projects/{projectId}/tasks";
    protected static final String TASK_ROUTE = "/api/tasks/{taskId}";
    protected static final String COMPLETED_TASK_ROUTE = "/api/completedTasks/{taskId}";
    protected static final String COMPLETE_TASK_ROUTE = "/api/tasks/{taskId}/complete";
    protected static final String UNCOMPLETE_TASK_ROUTE = "/api/tasks/{taskId}/uncomplete";
    protected static final String COMPLETED_TASKS_ROUTE = "/api/projects/{projectId}/completedTasks";
    protected static final String TASK_SET_LABELS_ROUTE = "/api/tasks/{taskId}/setLabels";
    protected static final String MOVE_TASK_ROUTE = "/api/tasks/{taskId}/move";
    protected static final String SHARE_TASK_ROUTE = "/api/tasks/{taskId}/share";
    protected static final String GET_SHARABLES_ROUTE = "/api/tasks/{taskId}/sharables";
    protected static final String REVOKE_SHARABLE_ROUTE = "/api/tasks/{taskId}/revokeSharable";
    protected static final String ADD_CONTENT_ROUTE = "/api/tasks/{taskId}/addContent";
    protected static final String CONTENT_ROUTE = "/api/tasks/{taskId}/contents/{contentId}";
    protected static final String CONTENTS_ROUTE = "/api/tasks/{taskId}/contents";
    protected static final String COMPLETED_TASK_CONTENTS_ROUTE = "/api/completedTasks/{taskId}/contents";
    protected static final String CONTENT_REVISIONS_ROUTE = "/api/tasks/{taskId}/contents/{contentId}/revisions/{revisionId}";

    @Autowired
    private TaskDaoJpa taskDaoJpa;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private SearchService searchService;

    @GetMapping(TASKS_ROUTE)
    public ResponseEntity<List<Task>> getTasks(@NotNull @PathVariable Long projectId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        List<Task> tasks = this.taskDaoJpa.getTasks(projectId, username)
                .stream().map(t -> addAvatar(t)).collect(Collectors.toList());
        String tasksEtag = EtagGenerator.generateEtag(EtagGenerator.HashAlgorithm.MD5,
                EtagGenerator.HashType.TO_HASHCODE, tasks);

        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.setETag(tasksEtag);

        return ResponseEntity.ok().headers(responseHeader).body(tasks);
    }

    private Task addAvatar(Task task) {
        task.setOwnerAvatar(this.userClient.getUser(task.getOwner()).getAvatar());
        task.getAssignees().forEach((assignee) -> {
            assignee.setAvatar(this.userClient.getUser(assignee.getName()).getAvatar());
        });
        if (task.getSubTasks() != null) {
            for (Task subTask : task.getSubTasks()) {
                addAvatar(subTask);
            }
        }
        return task;
    }

    @GetMapping(TASK_ROUTE)
    public Task getTask(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        return addAvatar(this.taskDaoJpa.getTask(username, taskId));
    }

    @GetMapping(COMPLETED_TASK_ROUTE)
    public Task getCompletedTask(@NotNull @PathVariable Long taskId) {

        String username = MDC.get(UserClient.USER_NAME_KEY);
        return addAvatar(this.taskDaoJpa.getCompletedTask(taskId, username).toPresentationModel());
    }

    @PostMapping(TASKS_ROUTE)
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@NotNull @PathVariable Long projectId,
                           @Valid @RequestBody CreateTaskParams params) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Task task = taskDaoJpa.create(projectId, username, params).toPresentationModel();
        searchService.saveToES(task);
        return task;
    }

    @PatchMapping(TASK_ROUTE)
    public ResponseEntity<List<Task>> updateTask(@NotNull @PathVariable Long taskId,
                                                 @Valid @RequestBody UpdateTaskParams updateTaskParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        List<Event> events = new ArrayList<>();
        Task task = this.taskDaoJpa.partialUpdate(
                username, taskId, updateTaskParams, events).toPresentationModel();
        if (!events.isEmpty()) {
            notificationService.inform(new UpdateTaskAssigneeEvent(events, username, updateTaskParams.getAssignees().get(0)));
        }
        return getTasks(task.getProjectId());
    }

    @PutMapping(TASKS_ROUTE)
    public ResponseEntity<List<Task>> updateTaskRelations(@NotNull @PathVariable Long projectId, @Valid @RequestBody List<Task> tasks) {
        this.taskDaoJpa.updateUserTasks(projectId, tasks);
        return getTasks(projectId);
    }

    @PostMapping(COMPLETE_TASK_ROUTE)
    public Task completeTask(@NotNull @PathVariable Long taskId,
                             @RequestBody Optional<String> dateTime) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        CompletedTask task = this.taskDaoJpa.complete(username, taskId, dateTime.orElse(null));
        return getCompletedTask(task.getId());
    }

    @PostMapping(UNCOMPLETE_TASK_ROUTE)
    public Task uncompleteTask(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Long newId = this.taskDaoJpa.uncomplete(username, taskId);
        return getTask(newId);
    }

    @GetMapping(COMPLETED_TASKS_ROUTE)
    public List<Task> getCompletedTasks(@NotNull @PathVariable Long projectId,
                                        @RequestParam(required = false, defaultValue = "0") Integer pageNo,
                                        @RequestParam(required = false, defaultValue = "50") Integer pageSize) {

        String username = MDC.get(UserClient.USER_NAME_KEY);
        return this.taskDaoJpa.getCompletedTasks(projectId, username, pageNo, pageSize)
                .stream().map(t -> addAvatar(t.toPresentationModel()))
                .collect(Collectors.toList());
    }

    @DeleteMapping(TASK_ROUTE)
    public ResponseEntity<List<Task>> deleteTask(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Task task = getTask(taskId);
        List<Event> events = this.taskDaoJpa.deleteTask(username, taskId);
        if (!events.isEmpty()) {
            this.notificationService.inform(new RemoveTaskEvent(events, username));
        }
        return getTasks(task.getProjectId());
    }

    @DeleteMapping(COMPLETED_TASK_ROUTE)
    public List<Task> deleteCompletedTask(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Task task = getCompletedTask(taskId);
        List<Event> events = this.taskDaoJpa.deleteCompletedTask(username, taskId);
        if (!events.isEmpty()) {
            this.notificationService.inform(new RemoveTaskEvent(events, username));
        }
        return getCompletedTasks(task.getProjectId(), 0, 50);
    }

    @PutMapping(TASK_SET_LABELS_ROUTE)
    public Task setLabels(@NotNull @PathVariable Long taskId,
                          @NotNull @RequestBody List<Long> labels) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.notificationService.inform(this.taskDaoJpa.setLabels(username, taskId, labels));
        return getTask(taskId);
    }

    @PostMapping(MOVE_TASK_ROUTE)
    public void moveTask(@NotNull @PathVariable Long taskId,
                         @NotNull @RequestBody MoveProjectItemParams moveProjectItemParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.taskDaoJpa.move(username, taskId, moveProjectItemParams.getTargetProject());
    }

    @PostMapping(SHARE_TASK_ROUTE)
    public SharableLink shareTask(
            @NotNull @PathVariable Long taskId,
            @NotNull @RequestBody ShareProjectItemParams shareProjectItemParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        if (shareProjectItemParams.isGenerateLink()) {
            return this.taskDaoJpa.generatePublicItemLink(
                    taskId, username, shareProjectItemParams.getTtl());
        }

        Informed inform = this.taskDaoJpa.shareProjectItem(taskId, shareProjectItemParams, username);
        this.notificationService.inform(inform);
        return null;
    }

    @GetMapping(GET_SHARABLES_ROUTE)
    public ProjectItemSharables getSharables(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        ProjectItemSharables result = this.taskDaoJpa.getSharables(taskId, username);
        List<User> users = result.getUsers().stream().map((u) -> this.userClient.getUser(u.getName()))
                .collect(Collectors.toList());
        result.setUsers(users);
        return result;
    }

    @PostMapping(REVOKE_SHARABLE_ROUTE)
    public void revokeSharable(
            @NotNull @PathVariable Long taskId,
            @NotNull @RequestBody RevokeProjectItemSharableParams revokeProjectItemSharableParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.taskDaoJpa.revokeSharable(taskId, username, revokeProjectItemSharableParams);
    }

    @PostMapping(ADD_CONTENT_ROUTE)
    public Content addContent(@NotNull @PathVariable Long taskId,
                              @NotNull @RequestBody CreateContentParams createContentParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        return this.taskDaoJpa.addContent(taskId, username, new TaskContent(createContentParams.getText()))
                .toPresentationModel();
    }

    @GetMapping(CONTENTS_ROUTE)
    public List<Content> getContents(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        return this.taskDaoJpa.getContents(taskId, username).stream()
                .map(t -> {
                    Content content = t.toPresentationModel();
                    content.setOwnerAvatar(this.userClient.getUser(content.getOwner()).getAvatar());
                    for (Revision revision : content.getRevisions()) {
                        revision.setUserAvatar(this.userClient.getUser(revision.getUser()).getAvatar());
                    }
                    return content;
                })
                .collect(Collectors.toList());
    }

    @GetMapping(COMPLETED_TASK_CONTENTS_ROUTE)
    public List<Content> getCompletedTaskContents(@NotNull @PathVariable Long taskId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        return this.taskDaoJpa.getCompletedTaskContents(taskId, username).stream()
                .map(t -> {
                    Content content = t.toPresentationModel();
                    content.setOwnerAvatar(this.userClient.getUser(content.getOwner()).getAvatar());
                    return content;
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping(CONTENT_ROUTE)
    public List<Content> deleteContent(@NotNull @PathVariable Long taskId,
                                       @NotNull @PathVariable Long contentId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.taskDaoJpa.deleteContent(contentId, taskId, username);
        return getContents(taskId);
    }

    @PatchMapping(CONTENT_ROUTE)
    public List<Content> updateContent(@NotNull @PathVariable Long taskId,
                                       @NotNull @PathVariable Long contentId,
                                       @NotNull @RequestBody UpdateContentParams updateContentParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.taskDaoJpa.updateContent(contentId, taskId, username, updateContentParams);
        return getContents(taskId);
    }

    @GetMapping(CONTENT_REVISIONS_ROUTE)
    public Revision getContentRevision(
            @NotNull @PathVariable Long taskId,
            @NotNull @PathVariable Long contentId,
            @NotNull @PathVariable Long revisionId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Revision revision = this.taskDaoJpa.getContentRevision(username, taskId, contentId, revisionId);
        revision.setUserAvatar(this.userClient.getUser(revision.getUser()).getAvatar());
        return revision;
    }
}