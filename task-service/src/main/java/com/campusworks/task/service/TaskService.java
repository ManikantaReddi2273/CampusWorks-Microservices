package com.campusworks.task.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusworks.task.client.BiddingServiceClient;
import com.campusworks.task.client.ProfileServiceClient;
import com.campusworks.task.model.Task;
import com.campusworks.task.repo.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Task Service
 * Handles business logic for task management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    private ProfileServiceClient profileServiceClient;
    
    @Autowired
    private BiddingServiceClient biddingServiceClient;
    
    @Value("${task.bidding-period-hours:24}")
    private int biddingPeriodHours;
    
    @Value("${task.max-description-length:1000}")
    private int maxDescriptionLength;
    
    @Value("${task.max-title-length:100}")
    private int maxTitleLength;
    
    @Value("${task.min-budget:1.0}")
    private BigDecimal minBudget;
    
    @Value("${task.max-budget:10000.0}")
    private BigDecimal maxBudget;
    
    /**
     * Create a new task
     */
    public Task createTask(Task task) {
        log.info("📝 Creating new task: {} by user: {}", task.getTitle(), task.getOwnerEmail());
        
        // Validate task data
        validateTaskData(task);
        
        // Set default values
        task.setStatus(Task.TaskStatus.OPEN);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // Set bidding deadline
        LocalDateTime biddingDeadline = LocalDateTime.now().plusHours(biddingPeriodHours);
        task.setBiddingDeadline(biddingDeadline);
        
        // Save task
        Task savedTask = taskRepository.save(task);
        
        log.info("✅ Task created successfully with ID: {} and bidding deadline: {}", 
                savedTask.getId(), savedTask.getBiddingDeadline());
        
        return savedTask;
    }
    
    /**
     * Get task by ID
     */
    public Optional<Task> getTaskById(Long id) {
        log.info("🔍 Retrieving task with ID: {}", id);
        
        Optional<Task> task = taskRepository.findById(id);
        
        if (task.isPresent()) {
            log.info("✅ Task found: {} (Status: {})", task.get().getTitle(), task.get().getStatus());
        } else {
            log.warn("❌ Task not found with ID: {}", id);
        }
        
        return task;
    }
    
    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        log.info("📋 Retrieving all tasks");
        
        List<Task> tasks = taskRepository.findAll();
        
        log.info("✅ Retrieved {} tasks", tasks.size());
        
        return tasks;
    }
    
    /**
     * Get tasks by owner ID
     */
    public List<Task> getTasksByOwnerId(Long ownerId) {
        log.info("👤 Retrieving tasks for owner ID: {}", ownerId);
        
        List<Task> tasks = taskRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        
        log.info("✅ Retrieved {} tasks for owner ID: {}", tasks.size(), ownerId);
        
        return tasks;
    }
    
    /**
     * Get tasks by assigned user ID
     */
    public List<Task> getTasksByAssignedUserId(Long assignedUserId) {
        log.info("👷 Retrieving tasks assigned to user ID: {}", assignedUserId);
        
        List<Task> tasks = taskRepository.findByAssignedUserIdOrderByCreatedAtDesc(assignedUserId);
        
        log.info("✅ Retrieved {} tasks assigned to user ID: {}", tasks.size(), assignedUserId);
        
        return tasks;
    }
    
    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        log.info("🏷️ Retrieving tasks with status: {}", status);
        
        List<Task> tasks = taskRepository.findByStatusOrderByCreatedAtDesc(status);
        
        log.info("✅ Retrieved {} tasks with status: {}", tasks.size(), status);
        
        return tasks;
    }
    
    /**
     * Get tasks by category
     */
    public List<Task> getTasksByCategory(Task.TaskCategory category) {
        log.info("📂 Retrieving tasks in category: {}", category);
        
        List<Task> tasks = taskRepository.findByCategoryOrderByCreatedAtDesc(category);
        
        log.info("✅ Retrieved {} tasks in category: {}", tasks.size(), category);
        
        return tasks;
    }
    
    /**
     * Get open tasks available for bidding
     */
    public List<Task> getOpenTasksForBidding() {
        log.info("🏷️ Retrieving open tasks available for bidding");
        
        List<Task> tasks = taskRepository.findOpenTasksForBidding(LocalDateTime.now());
        
        log.info("✅ Retrieved {} open tasks available for bidding", tasks.size());
        
        return tasks;
    }
    
    /**
     * Get tasks ready for assignment (bidding period ended)
     */
    public List<Task> getTasksReadyForAssignment() {
        log.info("⏰ Retrieving tasks ready for assignment (bidding period ended)");
        
        List<Task> tasks = taskRepository.findTasksReadyForAssignment(LocalDateTime.now());
        
        log.info("✅ Retrieved {} tasks ready for assignment", tasks.size());
        
        return tasks;
    }
    
    /**
     * Update task
     */
    public Task updateTask(Long taskId, Task updatedTask, Long userId) {
        log.info("✏️ Updating task ID: {} by user ID: {}", taskId, userId);
        
        // Check if task exists and user owns it
        Optional<Task> existingTaskOpt = taskRepository.findByIdAndOwnerId(taskId, userId);
        
        if (existingTaskOpt.isEmpty()) {
            log.warn("❌ Task not found or user not authorized to update task ID: {}", taskId);
            throw new RuntimeException("Task not found or you are not authorized to update it");
        }
        
        Task existingTask = existingTaskOpt.get();
        
        // Only allow updates if task is still open
        if (existingTask.getStatus() != Task.TaskStatus.OPEN) {
            log.warn("❌ Cannot update task ID: {} - status is: {}", taskId, existingTask.getStatus());
            throw new RuntimeException("Cannot update task - bidding period has ended");
        }
        
        // Update allowed fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setBudget(updatedTask.getBudget());
        existingTask.setCategory(updatedTask.getCategory());
        existingTask.setCompletionDeadline(updatedTask.getCompletionDeadline());
        existingTask.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate bidding deadline if needed
        if (updatedTask.getBiddingDeadline() != null) {
            existingTask.setBiddingDeadline(updatedTask.getBiddingDeadline());
        }
        
        // Save updated task
        Task savedTask = taskRepository.save(existingTask);
        
        log.info("✅ Task updated successfully: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Delete task
     */
    public void deleteTask(Long taskId, Long userId) {
        log.info("🗑️ Deleting task ID: {} by user ID: {}", taskId, userId);
        
        // Check if task exists and user owns it
        Optional<Task> taskOpt = taskRepository.findByIdAndOwnerId(taskId, userId);
        
        if (taskOpt.isEmpty()) {
            log.warn("❌ Task not found or user not authorized to delete task ID: {}", taskId);
            throw new RuntimeException("Task not found or you are not authorized to delete it");
        }
        
        Task task = taskOpt.get();
        
        // Only allow deletion if task is still open
        if (task.getStatus() != Task.TaskStatus.OPEN) {
            log.warn("❌ Cannot delete task ID: {} - status is: {}", taskId, task.getStatus());
            throw new RuntimeException("Cannot delete task - bidding period has ended");
        }
        
        taskRepository.deleteById(taskId);
        
        log.info("✅ Task deleted successfully: {} (ID: {})", task.getTitle(), taskId);
    }
    
    /**
     * Assign task to a user
     */
    public Task assignTask(Long taskId, Long assignedUserId, String assignedUserEmail) {
        log.info("👷 Assigning task ID: {} to user: {} ({})", taskId, assignedUserEmail, assignedUserId);
        
        try {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            
            if (taskOpt.isEmpty()) {
                log.warn("❌ Task not found with ID: {}", taskId);
                throw new RuntimeException("Task not found");
            }
            
            Task task = taskOpt.get();
            
            // Check if task can be assigned
            if (!task.canBeAssigned()) {
                log.warn("❌ Task ID: {} cannot be assigned - status: {}, bidding deadline: {}", 
                        taskId, task.getStatus(), task.getBiddingDeadline());
                throw new RuntimeException("Task cannot be assigned - bidding period has not ended or status is not OPEN");
            }
            
            // Check user availability via Profile Service
            try {
                // First get the profile by user ID, then check availability
                var profileResponse = profileServiceClient.getProfileByUserId(assignedUserId);
                if (profileResponse != null && !"AVAILABLE".equalsIgnoreCase(profileResponse.getAvailabilityStatus())) {
                    log.warn("❌ User {} is not available for work", assignedUserEmail);
                    throw new RuntimeException("User is not available for work");
                }
                log.info("✅ User {} availability confirmed via Profile Service", assignedUserEmail);
            } catch (Exception e) {
                log.warn("⚠️ Failed to check user availability via Profile Service: {}", e.getMessage());
                // Continue with assignment even if profile service check fails
            }
            
            // Assign task
            task.assignToUser(assignedUserId, assignedUserEmail);
            
            Task savedTask = taskRepository.save(task);
            
            log.info("✅ Task assigned successfully: {} to user: {}", savedTask.getTitle(), assignedUserEmail);
            
            return savedTask;
            
        } catch (Exception e) {
            log.error("❌ Error assigning task ID: {} to user: {}. Error: {}", taskId, assignedUserEmail, e.getMessage());
            throw new RuntimeException("Failed to assign task: " + e.getMessage());
        }
    }
    
    /**
     * Mark task as completed
     */
    public Task markTaskAsCompleted(Long taskId, Long assignedUserId) {
        log.info("✅ Marking task ID: {} as completed by assigned user: {}", taskId, assignedUserId);
        
        Optional<Task> taskOpt = taskRepository.findByIdAndAssignedUserId(taskId, assignedUserId);
        
        if (taskOpt.isEmpty()) {
            log.warn("❌ Task not found or user not assigned to task ID: {}", taskId);
            throw new RuntimeException("Task not found or you are not assigned to it");
        }
        
        Task task = taskOpt.get();
        
        // Check if task is in progress
        if (!task.isInProgress()) {
            log.warn("❌ Task ID: {} cannot be marked as completed - status: {}", taskId, task.getStatus());
            throw new RuntimeException("Task cannot be marked as completed - it is not in progress");
        }
        
        // Mark as completed
        task.markAsCompleted();
        
        Task savedTask = taskRepository.save(task);
        
        log.info("✅ Task marked as completed: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Accept completed task
     */
    public Task acceptTask(Long taskId, Long ownerId) {
        log.info("👍 Accepting completed task ID: {} by owner: {}", taskId, ownerId);
        
        Optional<Task> taskOpt = taskRepository.findByIdAndOwnerId(taskId, ownerId);
        
        if (taskOpt.isEmpty()) {
            log.warn("❌ Task not found or user not authorized to accept task ID: {}", taskId);
            throw new RuntimeException("Task not found or you are not authorized to accept it");
        }
        
        Task task = taskOpt.get();
        
        // Check if task is completed
        if (!task.isCompleted()) {
            log.warn("❌ Task ID: {} cannot be accepted - status: {}", taskId, task.getStatus());
            throw new RuntimeException("Task cannot be accepted - it is not completed");
        }
        
        // Accept task
        task.acceptTask();
        
        Task savedTask = taskRepository.save(task);
        
        log.info("✅ Task accepted successfully: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Cancel task
     */
    public Task cancelTask(Long taskId, Long ownerId) {
        log.info("❌ Cancelling task ID: {} by owner: {}", taskId, ownerId);
        
        Optional<Task> taskOpt = taskRepository.findByIdAndOwnerId(taskId, ownerId);
        
        if (taskOpt.isEmpty()) {
            log.warn("❌ Task not found or user not authorized to cancel task ID: {}", taskId);
            throw new RuntimeException("Task not found or you are not authorized to cancel it");
        }
        
        Task task = taskOpt.get();
        
        // Only allow cancellation if task is still open
        if (task.getStatus() != Task.TaskStatus.OPEN) {
            log.warn("❌ Task ID: {} cannot be cancelled - status: {}", taskId, task.getStatus());
            throw new RuntimeException("Task cannot be cancelled - bidding period has ended");
        }
        
        // Cancel task
        task.cancelTask();
        
        Task savedTask = taskRepository.save(task);
        
        log.info("✅ Task cancelled successfully: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Get tasks that need attention
     */
    public List<Task> getTasksNeedingAttention() {
        log.info("⚠️ Retrieving tasks that need attention");
        
        List<Task> tasks = taskRepository.findTasksNeedingAttention(LocalDateTime.now());
        
        log.info("✅ Retrieved {} tasks that need attention", tasks.size());
        
        return tasks;
    }
    
    /**
     * Get task statistics
     */
    public TaskStatistics getTaskStatistics() {
        log.info("📊 Retrieving task statistics");
        
        long totalTasks = taskRepository.count();
        long openTasks = taskRepository.countByStatus(Task.TaskStatus.OPEN);
        long inProgressTasks = taskRepository.countByStatus(Task.TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        long acceptedTasks = taskRepository.countByStatus(Task.TaskStatus.ACCEPTED);
        long cancelledTasks = taskRepository.countByStatus(Task.TaskStatus.CANCELLED);
        
        TaskStatistics stats = TaskStatistics.builder()
                .totalTasks(totalTasks)
                .openTasks(openTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .acceptedTasks(acceptedTasks)
                .cancelledTasks(cancelledTasks)
                .build();
        
        log.info("✅ Task statistics retrieved: {}", stats);
        
        return stats;
    }
    
    /**
     * Save task
     */
    public Task saveTask(Task task) {
        log.info("💾 Saving task: {} (ID: {})", task.getTitle(), task.getId());
        
        Task savedTask = taskRepository.save(task);
        
        log.info("✅ Task saved successfully: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Update task status
     */
    public Task updateTaskStatus(Long taskId, Task.TaskStatus newStatus) {
        log.info("🔄 Updating task status: ID: {} to: {}", taskId, newStatus);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        
        // Validate status transition
        validateStatusTransition(task.getStatus(), newStatus);
        
        // Update status
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        
        // Save and return
        Task updatedTask = taskRepository.save(task);
        
        log.info("✅ Task status updated successfully: ID: {}, New Status: {}", taskId, newStatus);
        
        return updatedTask;
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(Task.TaskStatus currentStatus, Task.TaskStatus newStatus) {
        // Add validation logic for status transitions if needed
        // For now, allow all transitions
        log.debug("Status transition: {} -> {}", currentStatus, newStatus);
    }
    
    /**
     * Validate task data
     */
    private void validateTaskData(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Task title is required");
        }
        
        if (task.getTitle().length() > maxTitleLength) {
            throw new RuntimeException("Task title cannot exceed " + maxTitleLength + " characters");
        }
        
        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Task description is required");
        }
        
        if (task.getDescription().length() > maxDescriptionLength) {
            throw new RuntimeException("Task description cannot exceed " + maxDescriptionLength + " characters");
        }
        
        if (task.getBudget() == null) {
            throw new RuntimeException("Task budget is required");
        }
        
        if (task.getBudget().compareTo(minBudget) < 0) {
            throw new RuntimeException("Task budget must be at least $" + minBudget);
        }
        
        if (task.getBudget().compareTo(maxBudget) > 0) {
            throw new RuntimeException("Task budget cannot exceed $" + maxBudget);
        }
        
        if (task.getCategory() == null) {
            throw new RuntimeException("Task category is required");
        }
        
        if (task.getOwnerId() == null) {
            throw new RuntimeException("Task owner ID is required");
        }
        
        if (task.getOwnerEmail() == null || task.getOwnerEmail().trim().isEmpty()) {
            throw new RuntimeException("Task owner email is required");
        }
    }
    
    /**
     * Check if a user is the owner of a specific task
     */
    public boolean isTaskOwner(Long taskId, Long userId) {
        log.info("🔍 Checking if user ID: {} is the owner of task ID: {}", userId, taskId);
        
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        
        if (taskOpt.isEmpty()) {
            log.warn("❌ Task not found with ID: {}", taskId);
            return false;
        }
        
        Task task = taskOpt.get();
        boolean isOwner = task.getOwnerId().equals(userId);
        
        log.info("✅ Task ownership check: User {} {} the owner of task {} (Owner ID: {})", 
                userId, isOwner ? "IS" : "IS NOT", taskId, task.getOwnerId());
        
        return isOwner;
    }
    
    /**
     * Task Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TaskStatistics {
        private long totalTasks;
        private long openTasks;
        private long inProgressTasks;
        private long completedTasks;
        private long acceptedTasks;
        private long cancelledTasks;
    }
}
