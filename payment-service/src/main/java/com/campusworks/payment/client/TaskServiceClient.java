package com.campusworks.payment.client;

import com.campusworks.payment.config.FeignClientConfig;
import com.campusworks.payment.dto.TaskResponse;
import com.campusworks.payment.dto.TaskStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "task-service", configuration = FeignClientConfig.class, fallback = TaskServiceClientFallback.class)
public interface TaskServiceClient {
    
    @GetMapping("/tasks/{taskId}")
    TaskResponse getTask(@PathVariable("taskId") Long taskId);
    
    @PutMapping("/tasks/{taskId}/status")
    ResponseEntity<?> updateTaskStatus(@PathVariable("taskId") Long taskId, 
                                     @RequestBody TaskStatusUpdateRequest request);
    
    @PostMapping("/tasks/{taskId}/reopen")
    ResponseEntity<?> reopenTask(@PathVariable("taskId") Long taskId);
    
    @PostMapping("/tasks/{taskId}/complete")
    ResponseEntity<?> markTaskAsCompleted(@PathVariable("taskId") Long taskId);
    
    @PostMapping("/tasks/{taskId}/fail")
    ResponseEntity<?> markTaskAsFailedAndReopen(@PathVariable("taskId") Long taskId);
}
