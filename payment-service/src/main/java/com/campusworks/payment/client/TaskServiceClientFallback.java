package com.campusworks.payment.client;

import com.campusworks.payment.dto.TaskResponse;
import com.campusworks.payment.dto.TaskStatusUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TaskServiceClientFallback implements TaskServiceClient {
    
    @Override
    public TaskResponse getTask(Long taskId) {
        log.error("🔥 Task Service unavailable - Using fallback for getTask({})", taskId);
        return TaskResponse.builder()
                .id(taskId)
                .title("Task Service Unavailable")
                .description("Unable to fetch task details")
                .taskDeadline(LocalDateTime.now().plusDays(7)) // Default 7 days for safety
                .success(false)
                .message("Task Service unavailable - Using fallback response")
                .build();
    }
    
    @Override
    public ResponseEntity<?> updateTaskStatus(Long taskId, TaskStatusUpdateRequest request) {
        log.error("🔥 Task Service unavailable - Using fallback for updateTaskStatus({}, {})", taskId, request.getStatus());
        return ResponseEntity.ok().body("Task Service unavailable - Status update queued for retry");
    }
    
    @Override
    public ResponseEntity<?> reopenTask(Long taskId) {
        log.error("🔥 Task Service unavailable - Using fallback for reopenTask({})", taskId);
        return ResponseEntity.ok().body("Task Service unavailable - Task reopen queued for retry");
    }
    
    @Override
    public ResponseEntity<?> markTaskAsCompleted(Long taskId) {
        log.error("🔥 Task Service unavailable - Using fallback for markTaskAsCompleted({})", taskId);
        return ResponseEntity.ok().body("Task Service unavailable - Task completion queued for retry");
    }
    
    @Override
    public ResponseEntity<?> markTaskAsFailedAndReopen(Long taskId) {
        log.error("🔥 Task Service unavailable - Using fallback for markTaskAsFailedAndReopen({})", taskId);
        return ResponseEntity.ok().body("Task Service unavailable - Task failure and reopen queued for retry");
    }
}
