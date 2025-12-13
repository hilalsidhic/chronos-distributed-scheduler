import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Layout } from "@/components/Layout";
import { StatsCard } from "@/components/StatsCard";
import { ExecutionsTable, JobExecution } from "@/components/ExecutionsTable";
import { Clock, CheckCircle2, XCircle, Activity, AlertTriangle, Loader2 } from "lucide-react";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { Job } from "@/components/JobsTable";
import { authService } from "@/auth/authService";
import { API_BASE_URL } from "@/config";

export default function Dashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [recentExecutions, setRecentExecutions] = useState<JobExecution[]>([]);
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    async function loadDashboard() {
      const token = authService.getToken();
      if (!token) {
        toast.error("Please login first");
        navigate("/login");
        return;
      }

      try {
        // 1. Fetch Jobs
        const jobsRes = await fetch(`${API_BASE_URL}/scheduler/jobs`, {
          headers: { "Authorization": token, "Content-Type": "application/json" }
        });
        if (jobsRes.status === 401) throw new Error("Session expired");
        const jobsData = await jobsRes.json();
        setJobs(jobsData);

        // 2. Fetch Stats
        const statsRes = await fetch(`${API_BASE_URL}/scheduler/executions/stats`, {
          headers: { "Authorization": token, "Content-Type": "application/json" }
        });
        if (statsRes.status === 401) throw new Error("Session expired");
        const statsData = await statsRes.json();
        
        setStats(statsData);
        setRecentExecutions(statsData.recentExecutions || []);

      } catch (err) {
        if (err instanceof Error) {
            if(err.message === "Session expired") {
                authService.logout();
                navigate("/login");
            } else {
                toast.error("Failed to load dashboard data");
            }
        }
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, [navigate]);

  if (loading || !stats) {
    return (
        <Layout>
            <div className="h-[50vh] flex items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
        </Layout>
    );
  }

  // --- Process Data ---
  const totalJobs = jobs.length;
  // Count active jobs based on 'isRecurring' or actual running status if you prefer
  const activeSchedules = jobs.filter((j) => j.isRecurring).length; 

  const totalSuccess = stats.totalSuccess ?? 0;
  const totalFailed = stats.totalFailed ?? 0;
  const totalTimedOut = stats.totalTimedOut ?? 0;
  const totalRunning = stats.totalRunning ?? 0; // Currently executing instances

  // Prepare Pie Chart Data
  const pieData = [
    { name: "Success", value: totalSuccess, color: "hsl(var(--success))" }, // Use CSS variable or hex #22c55e
    { name: "Failed", value: totalFailed, color: "hsl(var(--destructive))" }, // #ef4444
    { name: "Timed Out", value: totalTimedOut, color: "#f59e0b" }, // Amber
    { name: "Running", value: totalRunning, color: "#3b82f6" },   // Blue
  ].filter(item => item.value > 0); // Hide empty slices

  return (
    <Layout>
      <div className="space-y-8 animate-in fade-in duration-500">
        <div>
          <h1 className="text-4xl font-bold font-heading text-foreground">Dashboard</h1>
          <p className="text-muted-foreground mt-2">
            System health and execution summary
          </p>
        </div>

        {/* 1. Stats Grid - Expanded to 4 columns */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <StatsCard
            title="Total Schedules"
            value={totalJobs}
            icon={Clock}
            trend={`${activeSchedules} recurring`}
          />
          <StatsCard
            title="Success Rate"
            value={`${totalSuccess}`}
            icon={CheckCircle2}
            trend="Completed executions"
          />
          <StatsCard
            title="Failures"
            value={totalFailed}
            icon={XCircle}
            trend="Needs attention"
          />
           <StatsCard
            title="Timed Out"
            value={totalTimedOut}
            icon={AlertTriangle}
            trend="Exceeded limit"
          />
        </div>

        {/* 2. Visualization Row */}
        <div className="grid gap-6 md:grid-cols-7">
            {/* Chart takes up 3 columns */}
            <Card className="md:col-span-3">
                <CardHeader>
                    <CardTitle className="font-heading">Execution Status</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="h-[300px] w-full">
                        {pieData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={pieData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={60}
                                        outerRadius={80}
                                        paddingAngle={5}
                                        dataKey="value"
                                    >
                                        {pieData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <Tooltip 
                                        contentStyle={{ 
                                            backgroundColor: "hsl(var(--card))", 
                                            borderRadius: "8px", 
                                            border: "1px solid hsl(var(--border))" 
                                        }}
                                        itemStyle={{ color: "hsl(var(--foreground))" }}
                                    />
                                    <Legend verticalAlign="bottom" height={36}/>
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <div className="h-full flex items-center justify-center text-muted-foreground">
                                No execution data available yet.
                            </div>
                        )}
                    </div>
                </CardContent>
            </Card>

            {/* Recent Table takes up 4 columns */}
            <div className="md:col-span-4 space-y-4">
                <h2 className="text-2xl font-bold font-heading text-foreground">
                    Recent Activity
                </h2>
                {/* We wrap the table in a card effect by using the existing component container if needed, 
                    or just render it directly if ExecutionsTable has its own card style */}
                <ExecutionsTable executions={recentExecutions.slice(0, 5)} /> 
            </div>
        </div>
      </div>
    </Layout>
  );
}