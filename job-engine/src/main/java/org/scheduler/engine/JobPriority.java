package org.scheduler.engine;

/**
 * JobPriority defines the possible priority levels for a job.
 * In a priority-based scheduler, we use these values to determine 
 * the order of execution.
 */
public enum JobPriority {
    /**
     * URGENT has the highest priority (0). 
     * In a PriorityQueue, smaller numbers are typically served first.
     */
    URGENT(0),
    
    /**
     * HIGH priority (1).
     */
    HIGH(1),
    
    /**
     * MEDIUM priority (2).
     */
    MEDIUM(2),
    
    /**
     * LOW priority (3).
     */
    LOW(3);

    private final int level;

    /**
     * Constructor for the enum. 
     * Each priority is associated with a numerical level.
     * 
     * @param level The numerical priority level.
     */
    JobPriority(int level) {
        this.level = level;
    }

    /**
     * Gets the numerical level of this priority.
     * 
     * @return The priority level.
     */
    public int getLevel() {
        return level;
    }
}
