import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";

type JobStatus =
  | "PENDING"
  | "QUEUED"
  | "RUNNING"
  | "COMPLETED"
  | "FAILED"
  | "RESERVED";

type ExecutionStatus =
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "TIMED_OUT"
  | "STUCK";

interface StatusBadgeProps {
  status: JobStatus | ExecutionStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const variants: Record<string, string> = {
    PENDING: "bg-muted text-muted-foreground",
    QUEUED: "bg-muted text-muted-foreground",

    RUNNING: "bg-primary/10 text-primary border-primary/20",

    COMPLETED: "bg-success/10 text-success border-success/20",
    SUCCESS: "bg-success/10 text-success border-success/20",

    FAILED: "bg-destructive/10 text-destructive border-destructive/20",

    TIMED_OUT: "bg-warning/10 text-warning border-warning/20",
    STUCK: "bg-warning/10 text-warning border-warning/20",

    RESERVED: "bg-blue-100 text-blue-700 border-blue-300",
  };

  // fallback if backend sends unexpected status
  const fallback = "bg-muted/50 text-muted-foreground border-muted";

  return (
    <Badge
      variant="outline"
      className={cn(
        "font-mono text-xs font-medium",
        variants[status] ?? fallback,
        className
      )}
    >
      {status}
    </Badge>
  );
}
