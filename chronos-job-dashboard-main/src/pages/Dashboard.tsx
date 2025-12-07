import { useEffect, useState } from "react";
import { Layout } from "@/components/Layout";
import { StatsCard } from "@/components/StatsCard";
import { ExecutionsTable, JobExecution } from "@/components/ExecutionsTable";
import { Clock, CheckCircle2, XCircle, Activity } from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { Job } from "@/components/JobsTable";
import { format } from "date-fns";

export default function Dashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [executions, setExecutions] = useState<JobExecution[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDashboard() {
      try {
        // Load all jobs
        const jobsRes = await fetch("http://localhost:8080/jobs");
        if (!jobsRes.ok) throw new Error("Failed to load jobs");
        const jobsData: Job[] = await jobsRes.json();
        setJobs(jobsData);

        // Load executions for all jobs
        const executionsRes = await fetch(
          "http://localhost:8080/executions/?limit=100&offset=0"
        );
        if (!executionsRes.ok) throw new Error("Failed to load executions");

        const allExecutions: JobExecution[] = await executionsRes.json();

        // Sort by newest first
        allExecutions.sort(
          (a, b) =>
            new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
        );

        setExecutions(allExecutions);
      } catch (err) {
        toast.error("Failed to load dashboard data");
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, []);

  if (loading) return <Layout>Loading dashboard…</Layout>;

  // --- Stats ---
  const totalJobs = jobs.length;
  const runningJobs = jobs.filter((j) => j.status === "RUNNING").length;
  const successfulExecutions = executions.filter((e) => e.status === "SUCCESS").length;
  const failedExecutions = executions.filter((e) => e.status === "FAILED").length;

  // --- Chart Data (last 7 days) ---
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const chartData = Array.from({ length: 7 }, (_, i) => {
    const dayIndex = (new Date().getDay() - i + 7) % 7;
    const dayName = days[dayIndex];

    const dailyExecutions = executions.filter((e) => {
      const d = new Date(e.startedAt).getDay();
      return d === dayIndex;
    });

    return {
      name: dayName,
      successful: dailyExecutions.filter((e) => e.status === "SUCCESS").length,
      failed: dailyExecutions.filter((e) => e.status === "FAILED").length,
    };
  }).reverse();

  // Recent executions → take last 10
  const recentExecutions = executions.slice(0, 10);

  return (
    <Layout>
      <div className="space-y-8 animate-in fade-in duration-500">
        {/* Header */}
        <div>
          <h1 className="text-4xl font-bold font-heading text-foreground">Dashboard</h1>
          <p className="text-muted-foreground mt-2">
            Monitor your distributed job scheduler at a glance
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <StatsCard
            title="Total Jobs"
            value={totalJobs}
            icon={Clock}
            trend={`${totalJobs} total jobs`}
          />
          <StatsCard
            title="Running Jobs"
            value={runningJobs}
            icon={Activity}
            trend="Currently active"
          />
          <StatsCard
            title="Successful Executions"
            value={successfulExecutions}
            icon={CheckCircle2}
            trend={`${successfulExecutions} successful`}
          />
          <StatsCard
            title="Failed Executions"
            value={failedExecutions}
            icon={XCircle}
            trend={`${failedExecutions} failed`}
          />
        </div>

        {/* Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="font-heading">Execution History</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="name" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "hsl(var(--card))",
                    border: "1px solid hsl(var(--border))",
                    borderRadius: "0.75rem",
                  }}
                />
                <Legend />
                <Bar dataKey="successful" fill="hsl(var(--success))" name="Successful" radius={[8, 8, 0, 0]} />
                <Bar dataKey="failed" fill="hsl(var(--destructive))" name="Failed" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Recent Executions */}
        <div>
          <h2 className="text-2xl font-bold font-heading text-foreground mb-4">
            Recent Executions
          </h2>
          <ExecutionsTable executions={recentExecutions} />
        </div>
      </div>
    </Layout>
  );
}
