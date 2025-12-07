import { useState, useEffect } from "react";
import { Layout } from "@/components/Layout";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { StatusBadge } from "@/components/StatusBadge";
import { ExecutionsTable, JobExecution } from "@/components/ExecutionsTable";
import { ArrowLeft, Clock, Trash2, PlayCircle } from "lucide-react";
import { format } from "date-fns";
import { Job } from "@/components/JobsTable";
import { toast } from "sonner";

export default function JobDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [job, setJob] = useState<Job | null>(null);
  const [executions, setExecutions] = useState<JobExecution[]>([]);
  const [loading, setLoading] = useState(true);

  // Load job + executions
  useEffect(() => {
    if (!id) return;

    async function fetchJobData() {
      try {
        // Fetch job
        const jobRes = await fetch(`http://localhost:8080/jobs/${id}`);
        if (!jobRes.ok) throw new Error("Failed to load job");
        const jobData = await jobRes.json();

        // Fetch executions
        const execRes = await fetch(
          `http://localhost:8080/jobs/${id}/executions?limit=20&offset=0`
        );
        if (!execRes.ok) throw new Error("Failed to load executions");
        const execData = await execRes.json();

        setJob(jobData);
        setExecutions(execData);
      } catch (err) {
        toast.error("Failed to load job details");
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    fetchJobData();
  }, [id]);

  const handleDelete = async () => {
    try {
      const res = await fetch(`http://localhost:8080/jobs/${id}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error();
      toast.success("Job deleted successfully");
      navigate("/jobs");
    } catch {
      toast.error("Failed to delete job");
    }
  };

  const handleTrigger = () => {
    toast.success("Job triggered successfully");
  };

  if (loading) return <Layout>Loading job…</Layout>;
  if (!job) return <Layout>Job not found</Layout>;

  return (
    <Layout>
      <div className="space-y-6 animate-in fade-in duration-500">
        {/* Header */}
        <div>
          <Button
            variant="ghost"
            onClick={() => navigate("/jobs")}
            className="mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Jobs
          </Button>
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-4xl font-bold font-heading text-foreground">
                {job.name}
              </h1>
              <p className="text-muted-foreground mt-2">Job #{id}</p>
            </div>
            <div className="flex gap-2">
              <Button onClick={handleTrigger} variant="outline">
                <PlayCircle className="h-4 w-4 mr-2" />
                Trigger Now
              </Button>
              <Button onClick={handleDelete} variant="outline">
                <Trash2 className="h-4 w-4 mr-2 text-destructive" />
                Delete
              </Button>
            </div>
          </div>
        </div>

        {/* Job Details Cards */}
        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle className="font-heading">Job Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Status</span>
                <StatusBadge status={job.status} />
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Type</span>
                <Badge variant="outline" className="gap-1">
                  {job.recurring && <Clock className="h-3 w-3" />}
                  {job.recurring ? "Recurring" : "One-time"}
                </Badge>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Created By</span>
                <span className="text-sm font-medium">{job.createdBy}</span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Created At</span>
                <span className="text-sm font-medium font-mono">
                  {format(new Date(job.createdAt), "MMM dd, yyyy HH:mm")}
                </span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="font-heading">Execution Configuration</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {job.recurring && (
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium text-muted-foreground">Interval</span>
                  <span className="text-sm font-medium font-mono">
                    {job.intervalSeconds}s
                  </span>
                </div>
              )}

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Next Execution</span>
                <span className="text-sm font-medium font-mono">
                  {format(new Date(job.nextExecutionTime), "MMM dd, HH:mm")}
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">Max Retries</span>
                <span className="text-sm font-medium font-mono">
                  {job.maxRetry}
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-muted-foreground">
                  Max Execution Time
                </span>
                <span className="text-sm font-medium font-mono">
                  {job.maxExecutionTime}s
                </span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Payload */}
        <Card>
          <CardHeader>
            <CardTitle className="font-heading">Payload</CardTitle>
          </CardHeader>
          <CardContent>
            <pre className="p-4 bg-muted rounded-lg overflow-auto text-sm font-mono">
              {JSON.stringify(job.payload, null, 2)}
            </pre>
          </CardContent>
        </Card>

        {/* Executions */}
        <div>
          <h2 className="text-2xl font-bold font-heading text-foreground mb-4">
            Execution History
          </h2>

          <ExecutionsTable executions={executions} showJobName={false} />
        </div>
      </div>
    </Layout>
  );
}
