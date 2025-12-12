import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom"; // Import useNavigate
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
import { authService } from "@/auth/authService"; // Import authService
import { API_BASE_URL } from "@/config";

export default function Executions() {
  const [executions, setExecutions] = useState<JobExecution[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [loading, setLoading] = useState(true);

  // Pagination states
  const [page, setPage] = useState(0);
  const [limit, setLimit] = useState(50);
  const [hasNext, setHasNext] = useState(false);
  const [total, setTotal] = useState(0);

  const navigate = useNavigate();

  useEffect(() => {
    async function loadExecutions() {
      // 2. Get Token
      const token = authService.getToken();

      // 3. Redirect if not authenticated
      if (!token) {
        toast.error("Please login first");
        navigate("/login");
        return;
      }

      try {
        setLoading(true);

        // 4. Update Fetch URL to use dynamic API_BASE_URL
        const executionsRes = await fetch(
          `${API_BASE_URL}/scheduler/executions/?limit=${limit}&offset=${page * limit}`,
          {
            method: "GET",
            headers: {
              "Authorization": token,
              "Content-Type": "application/json"
            }
          }
        );

        // 5. Handle 401 (Session Expired)
        if (executionsRes.status === 401) {
            authService.logout();
            navigate("/login");
            throw new Error("Session expired");
        }

        if (!executionsRes.ok) throw new Error("Failed to load executions");

        const data = await executionsRes.json();

        // If backend returns a list → fallback
        if (Array.isArray(data)) {
          setExecutions(data);
          setHasNext(false);
          setTotal(data.length);
        } else {
          // If backend returns paginated format
          setExecutions(data.items || []);
          setHasNext(data.hasNext ?? false);
          setTotal(data.total ?? 0);
        }

      } catch (err) {
        // Only show error toast if it wasn't a redirect
        if (err instanceof Error && err.message !== "Session expired") {
            toast.error("Failed to load execution history");
            console.error(err);
        }
      } finally {
        setLoading(false);
      }
    }

    loadExecutions();
  }, [page, limit, navigate, API_BASE_URL]); // Added API_BASE_URL to dependencies

  // Client-side filtering (search & status)
  const filteredExecutions = executions.filter((execution) => {
    const matchesSearch = execution.jobName
      .toLowerCase()
      .includes(searchQuery.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || execution.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  if (loading) return <Layout>Loading executions...</Layout>;

  return (
    <Layout>
      <div className="space-y-6 animate-in fade-in duration-500">
        {/* Header */}
        <div>
          <h1 className="text-4xl font-bold font-heading text-foreground">
            Executions
          </h1>
          <p className="text-muted-foreground mt-2">
            View all job execution history with pagination
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

          {/* Page size selector */}
          <Select value={String(limit)} onValueChange={(v) => setLimit(Number(v))}>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Rows per page" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="25">25</SelectItem>
              <SelectItem value="50">50</SelectItem>
              <SelectItem value="100">100</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Executions Table */}
        <ExecutionsTable executions={filteredExecutions} />

        {/* Pagination Controls */}
        <div className="flex justify-between items-center pt-4">
          <button
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
            className="px-4 py-2 bg-muted hover:bg-muted/80 rounded-md disabled:opacity-50"
          >
            Previous
          </button>

          <p className="text-muted-foreground">
            Page {page + 1} • Showing {filteredExecutions.length} of {total}
          </p>

          <button
            disabled={!hasNext}
            onClick={() => setPage(page + 1)}
            className="px-4 py-2 bg-muted hover:bg-muted/80 rounded-md disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </Layout>
  );
}