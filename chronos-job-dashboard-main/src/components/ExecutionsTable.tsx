import { useState } from "react";
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
import { FileText, ChevronLeft, ChevronRight } from "lucide-react";
import { format } from "date-fns";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";

export interface JobExecution {
  id: number;
  jobId: number;
  jobName: string;
  status: "RUNNING" | "SUCCESS" | "FAILED" | "TIMED_OUT" | "STUCK";
  startedAt: string;
  finishedAt: string | null;
  log: string;
}

interface ExecutionsTableProps {
  executions: JobExecution[];
  showJobName?: boolean;
  onPageChange?: (page: number) => void;
  currentPage?: number;
  totalPages?: number;
}

export function ExecutionsTable({
  executions,
  showJobName = true,
  onPageChange,
  currentPage = 1,
  totalPages = 1,
}: ExecutionsTableProps) {
  const [selectedLog, setSelectedLog] = useState<JobExecution | null>(null);

  const formatDuration = (start: string, end: string | null) => {
    if (!end) return "In progress";
    const diff = new Date(end).getTime() - new Date(start).getTime();
    const seconds = Math.floor(diff / 1000);
    return `${seconds}s`;
  };

  return (
    <>
      <div className="rounded-xl border border-border bg-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50 hover:bg-muted/50">
              <TableHead className="font-heading">ID</TableHead>
              {showJobName && <TableHead className="font-heading">Job Name</TableHead>}
              <TableHead className="font-heading">Status</TableHead>
              <TableHead className="font-heading">Started At</TableHead>
              <TableHead className="font-heading">Duration</TableHead>
              <TableHead className="font-heading text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {executions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={showJobName ? 6 : 5} className="text-center py-8 text-muted-foreground">
                  No executions found.
                </TableCell>
              </TableRow>
            ) : (
              executions.map((execution) => (
                <TableRow key={execution.id} className="hover:bg-muted/30 transition-colors">
                  <TableCell className="font-mono text-sm">{execution.id}</TableCell>
                  {showJobName && (
                    <TableCell className="font-medium">{execution.jobName}</TableCell>
                  )}
                  <TableCell>
                    <StatusBadge status={execution.status} />
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {format(new Date(execution.startedAt), "MMM dd, HH:mm:ss")}
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {formatDuration(execution.startedAt, execution.finishedAt)}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setSelectedLog(execution)}
                    >
                      <FileText className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-4">
          <p className="text-sm text-muted-foreground">
            Page {currentPage} of {totalPages}
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange?.(currentPage - 1)}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="h-4 w-4" />
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange?.(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      <Dialog open={selectedLog !== null} onOpenChange={() => setSelectedLog(null)}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle className="font-heading">Execution Log</DialogTitle>
            <DialogDescription>
              Execution #{selectedLog?.id} - {selectedLog?.jobName}
            </DialogDescription>
          </DialogHeader>
          <ScrollArea className="h-96 w-full rounded-lg border border-border bg-muted/50 p-4">
            <pre className="text-xs font-mono whitespace-pre-wrap">
              {selectedLog?.log || "No log available"}
            </pre>
          </ScrollArea>
        </DialogContent>
      </Dialog>
    </>
  );
}
