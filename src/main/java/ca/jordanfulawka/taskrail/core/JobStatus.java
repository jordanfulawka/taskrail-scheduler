package ca.jordanfulawka.taskrail.core;

public enum JobStatus {
    PENDING, RUNNING, SUCCEEDED, FAILED, DEAD, LEASE_EXPIRED;

    public static boolean isValidJobStatus(String status) {
        for(JobStatus jobStatus : values()) {
            if(jobStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}
