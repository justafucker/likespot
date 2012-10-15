package jobs;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * @author Denis Davydov
 */
@OnApplicationStart(async=true)
public class JobScheduler extends Job {

    @Override
    public void doJob() throws Exception {
        new EmailJob().every(Play.configuration.getProperty("jobs.emailjob.every", "15min"));
    }
}
