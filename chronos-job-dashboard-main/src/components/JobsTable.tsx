import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "./StatusBadge";
import { Eye, Trash2, Clock } from "lucide-react";
import { format } from "date-fns";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

export interface Job {
  id: number;
  name: string;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED" | "RESERVED";
  payload: Record<string, any>;
  isRecurring?: boolean; 
  recurring?: boolean; 
  intervalSeconds: number;
  nextExecutionTime: string;
  maxRetry: number;
  maxExecutionTime: number;
  isEnabled: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

interface JobsTableProps {
  jobs: Job[];
  onDelete?: (id: number) => void;
}

export function JobsTable({ jobs, onDelete }: JobsTableProps) {
  const navigate = useNavigate();
  const [deleteJobId, setDeleteJobId] = useState<number | null>(null);

  const handleDelete = () => {
    if (deleteJobId && onDelete) {
      onDelete(deleteJobId);
      setDeleteJobId(null);
    }
  };

  return (
    <>
      <div className="rounded-xl border border-border bg-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50 hover:bg-muted/50">
              <TableHead className="font-heading">ID</TableHead>
              <TableHead className="font-heading">Name</TableHead>
              <TableHead className="font-heading">Status</TableHead>
              <TableHead className="font-heading">Type</TableHead>
              <TableHead className="font-heading">Next Execution</TableHead>
              <TableHead className="font-heading">Created By</TableHead>
              <TableHead className="font-heading text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {jobs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  No jobs found. Create your first job to get started.
                </TableCell>
              </TableRow>
            ) : (
              jobs.map((job) => (
                <TableRow key={job.id} className="hover:bg-muted/30 transition-colors">
                  <TableCell className="font-mono text-sm">{job.id}</TableCell>
                  <TableCell className="font-medium">{job.name}</TableCell>
                  <TableCell>
                    <StatusBadge status={job.status} />
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {job.recurring && <Clock className="h-4 w-4 text-primary" />}
                      <span className="text-sm">
                        {job.recurring ? "Recurring" : "One-time"}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {format(new Date(job.nextExecutionTime), "MMM dd, HH:mm")}
                  </TableCell>
                  <TableCell className="text-sm">{job.createdBy}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => navigate(`/jobs/${job.id}`)}
                      >
                        <Eye className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setDeleteJobId(job.id)}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <AlertDialog open={deleteJobId !== null} onOpenChange={() => setDeleteJobId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Job</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this job? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/90">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
