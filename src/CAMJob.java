

public class CAMJob {
	private String jobname;
	private int jobID;
	
	public CAMJob(String jobname, int jobID) {
		this.jobname = jobname;
		this.jobID = jobID;
	}

	public String getJobname() {
		return jobname;
	}

	public void setJobname(String jobname) {
		this.jobname = jobname;
	}

	public int getJobID() {
		return jobID;
	}

	public void setJobID(int jobID) {
		this.jobID = jobID;
	}
	
	public String toString() {
		return this.jobname;
	}
}
