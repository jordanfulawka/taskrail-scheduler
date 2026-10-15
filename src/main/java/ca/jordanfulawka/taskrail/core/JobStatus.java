package ca.jordanfulawka.taskrail.core;

public enum JobStatus {
    PENDING, RUNNING, SUCCEEDED, FAILED;

    public static boolean isValidJobStatus(String status) {
        for(JobStatus jobStatus : values()) {
            if(jobStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}
