import { useState,useEffect } from "react";
import { Layout } from "@/components/Layout";
import { JobsTable, Job } from "@/components/JobsTable";
import { Button } from "@/components/ui/button";
import { Plus, Search } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";


export default function Jobs() {
  const navigate = useNavigate();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("http://localhost:8080/jobs")
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch jobs");
        return res.json();
      })
      .then((data) => setJobs(data))
      .catch((err) => {
        toast.error("Error loading jobs");
        console.error(err);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = (id: number) => {
    fetch(`http://localhost:8080/jobs/${id}`, {
      method: "DELETE",
    })
      .then((res) => {
        if (!res.ok) throw new Error("Delete failed");
        setJobs((prev) => prev.filter((job) => job.id !== id));
        toast.success("Job deleted successfully");
      })
      .catch(() => toast.error("Failed to delete job"));
  };

  const filteredJobs = jobs.filter((job) => {
    const matchesSearch = job.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === "all" || job.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (loading) return <Layout>Loading jobs...</Layout>;

  return (
    <Layout>
      <div className="space-y-6 animate-in fade-in duration-500">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold font-heading text-foreground">
              Jobs
            </h1>
            <p className="text-muted-foreground mt-2">
              Manage and monitor all scheduled jobs
            </p>
          </div>
          <Button onClick={() => navigate("/jobs/new")} size="lg">
            <Plus className="h-5 w-5 mr-2" />
            Create Job
          </Button>
        </div>

        {/* Filters */}
        <div className="flex gap-4 items-center">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search jobs..."
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
              <SelectItem value="QUEUED">Queued</SelectItem>
              <SelectItem value="RUNNING">Running</SelectItem>
              <SelectItem value="COMPLETED">Completed</SelectItem>
              <SelectItem value="FAILED">Failed</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Jobs Table */}
        <JobsTable jobs={filteredJobs} onDelete={handleDelete} />
      </div>
    </Layout>
  );
}
