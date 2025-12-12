import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // Assuming you use react-router
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
import { authService } from "@/auth/authService";

export default function Dashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [recentExecutions, setRecentExecutions] = useState<JobExecution[]>([]);
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  
  // Use navigation to redirect if not logged in
  const navigate = useNavigate();

  useEffect(() => {
    async function loadDashboard() {
      // 1. Get Token from AuthService
      const token = authService.getToken();

      // 2. Redirect if not authenticated
      if (!token) {
        toast.error("Please login first");
        navigate("/login");
        return;
      }

      try {
        // 3. Add Token to Jobs Request
        const jobsRes = await fetch("http://localhost:8080/scheduler/jobs", {
          method: "GET",
          headers: {
            "Authorization": token, // Uses the stored Basic Auth string
            "Content-Type": "application/json"
          }
        });

        // Handle Session Expiry (401)
        if (jobsRes.status === 401) {
            authService.logout();
            navigate("/login");
            throw new Error("Session expired");
        }

        if (!jobsRes.ok) throw new Error("Failed to load jobs");
        
        const jobsData: Job[] = await jobsRes.json();
        setJobs(jobsData);

        // 4. Add Token to Stats Request
        const statsRes = await fetch("http://localhost:8080/scheduler/executions/stats", {
          method: "GET",
          headers: {
            "Authorization": token,
            "Content-Type": "application/json"
          }
        });

        if (statsRes.status === 401) {
            authService.logout();
            navigate("/login");
            throw new Error("Session expired");
        }

        if (!statsRes.ok) throw new Error("Failed to load execution stats");
        
        const statsData = await statsRes.json();

        setStats(statsData);
        setRecentExecutions(statsData.recentExecutions || []);

      } catch (err) {
        // Don't show toast if we already redirected for 401
        if (err instanceof Error && err.message !== "Session expired") {
            toast.error("Failed to load dashboard data");
            console.error(err);
        }
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, [navigate]);

  if (loading || !stats) return <Layout>Loading dashboard...</Layout>;

  // --- Stats ---
  const totalJobs = jobs.length;
  const runningJobs = jobs.filter((j) => j.status === "RUNNING").length;

  const totalSuccess = stats.totalSuccess ?? 0;
  const totalFailed = stats.totalFailed ?? 0;
  const totalRunning = stats.totalRunning ?? 0;
  const totalTimedOut = stats.totalTimedOut ?? 0;
  const totalStuck = stats.totalStuck ?? 0;

  // --- Chart Data from recent executions (last 7 days only) ---
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const chartData = Array.from({ length: 7 }, (_, i) => ({
    name: days[(new Date().getDay() - i + 7) % 7],
    successful: stats.totalSuccess > 0 ? Math.floor(stats.totalSuccess / 7) : 0,
    failed: stats.totalFailed > 0 ? Math.floor(stats.totalFailed / 7) : 0,
  })).reverse();


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
            value={totalSuccess}
            icon={CheckCircle2}
            trend={`${totalSuccess} successful`}
          />
          <StatsCard
            title="Failed Executions"
            value={totalFailed}
            icon={XCircle}
            trend={`${totalFailed} failed`}
          />
        </div>

        {/* Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="font-heading">Execution History (Last 7 days)</CardTitle>
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