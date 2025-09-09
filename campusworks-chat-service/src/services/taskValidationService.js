const logger = require('../utils/logger');

class TaskValidationService {
  constructor() {
    this.taskCache = new Map(); // Simple in-memory cache
    this.cacheTimeout = 5 * 60 * 1000; // 5 minutes
  }

  /**
   * Validate task access for chat without requiring Spring Boot service
   * @param {number} taskId - Task ID
   * @param {number} userId - User ID
   * @param {string} userEmail - User email
   * @returns {Promise<Object>} Validation result
   */
  async validateTaskAccessForChat(taskId, userId, userEmail) {
    try {
      // Check cache first
      const cacheKey = `${taskId}-${userId}`;
      const cached = this.taskCache.get(cacheKey);
      
      if (cached && (Date.now() - cached.timestamp) < this.cacheTimeout) {
        logger.info('Using cached task validation', { taskId, userId });
        return cached.result;
      }

      // For now, allow access to any task for chat purposes
      // This is a fallback when Spring Boot service is not available
      const result = {
        valid: true,
        error: null,
        task: {
          id: taskId,
          title: `Task ${taskId}`,
          status: 'IN_PROGRESS', // Assume in progress for chat
          ownerId: userId, // Assume current user is owner
          ownerEmail: userEmail,
          assignedUserId: null, // Will be updated when task is assigned
          assignedUserEmail: null
        }
      };

      // Cache the result
      this.taskCache.set(cacheKey, {
        result,
        timestamp: Date.now()
      });

      logger.info('Task validation successful (fallback)', { taskId, userId });
      return result;

    } catch (error) {
      logger.error('Task validation failed (fallback)', { 
        taskId, 
        userId, 
        error: error.message 
      });
      
      return {
        valid: false,
        error: 'Task validation failed',
        task: null
      };
    }
  }

  /**
   * Clear cache for a specific task
   * @param {number} taskId - Task ID
   */
  clearTaskCache(taskId) {
    for (const [key, value] of this.taskCache.entries()) {
      if (key.startsWith(`${taskId}-`)) {
        this.taskCache.delete(key);
      }
    }
  }

  /**
   * Clear all cache
   */
  clearAllCache() {
    this.taskCache.clear();
  }
}

module.exports = new TaskValidationService();
