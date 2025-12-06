import { useState, useEffect } from "react";
import { Layout } from "@/components/Layout";
import { ExecutionsTable, JobExecution } from "@/components/ExecutionsTable";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { Job } from "@/components/JobsTable";

export default function Executions() {
  const [executions, setExecutions] = useState<JobExecution[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadExecutions() {
      try {
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
        toast.error("Failed to load execution history");
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    loadExecutions();
  }, []);

  const filteredExecutions = executions.filter((execution) => {
    const matchesSearch = execution.jobName
      .toLowerCase()
      .includes(searchQuery.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || execution.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  if (loading) return <Layout>Loading executions…</Layout>;

  return (
    <Layout>
      <div className="space-y-6 animate-in fade-in duration-500">
        {/* Header */}
        <div>
          <h1 className="text-4xl font-bold font-heading text-foreground">
            Executions
          </h1>
          <p className="text-muted-foreground mt-2">
            View all job execution history and logs
          </p>
        </div>

        {/* Filters */}
        <div className="flex gap-4 items-center">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search executions..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>

          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-48">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="RUNNING">Running</SelectItem>
              <SelectItem value="SUCCESS">Success</SelectItem>
              <SelectItem value="FAILED">Failed</SelectItem>
              <SelectItem value="TIMED_OUT">Timed Out</SelectItem>
              <SelectItem value="STUCK">Stuck</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Executions Table */}
        <ExecutionsTable executions={filteredExecutions} />
      </div>
    </Layout>
  );
}
